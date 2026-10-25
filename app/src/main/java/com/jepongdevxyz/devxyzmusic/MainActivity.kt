package com.jepongdevxyz.devxyzmusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private fun hasAudioPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val context = LocalContext.current
                    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
                    var pickedSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
                    var favorites by remember {
                        mutableStateOf(FavoritesStore.load(context))
                    }
                    var hasPermission by remember { mutableStateOf(hasAudioPermission()) }
                    var showPlayer by remember { mutableStateOf(false) }

                    val queue by PlayerState.queue.collectAsState()
                    val index by PlayerState.index.collectAsState()
                    val isPlaying by PlayerState.isPlaying.collectAsState()
                    val currentSong = queue.getOrNull(index)

                    LaunchedEffect(favorites) {
                        PlayerState.favorites.value = favorites
                    }

                    LaunchedEffect(hasPermission) {
                        if (hasPermission) {
                            songs = withContext(Dispatchers.IO) {
                                MusicRepository.loadFromMediaStore(context)
                            }
                        }
                    }

                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        hasPermission = granted
                    }

                    val pickLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenMultipleDocuments()
                    ) { uris ->
                        val newPicked = uris.mapNotNull { uri ->
                            try {
                                contentResolver.takePersistableUriPermission(
                                    uri,
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: Exception) {
                                // Persistable permission is best-effort; playback still works this session.
                            }
                            MusicRepository.songFromPickedFile(context, uri)
                        }
                        if (newPicked.isNotEmpty()) {
                            pickedSongs = (pickedSongs + newPicked).distinctBy { it.uri }
                        }
                    }

                    val allSongs = pickedSongs + songs

                    val toggleFavorite: (Song) -> Unit = { song ->
                        val updated = if (favorites.contains(song.id)) {
                            favorites - song.id
                        } else {
                            favorites + song.id
                        }
                        favorites = updated
                        FavoritesStore.save(context, updated)
                    }

                    if (showPlayer) {
                        PlayerScreen(
                            onClose = { showPlayer = false },
                            onToggleFavorite = { song -> toggleFavorite(song) }
                        )
                    } else {
                        LibraryScreen(
                            songs = allSongs,
                            favorites = favorites,
                            isPlaying = isPlaying,
                            currentSong = currentSong,
                            hasPermission = hasPermission,
                            onRequestPermission = {
                                val permission = if (Build.VERSION.SDK_INT >= 33) {
                                    Manifest.permission.READ_MEDIA_AUDIO
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                permissionLauncher.launch(permission)
                            },
                            onPickFiles = {
                                pickLauncher.launch(arrayOf("audio/*"))
                            },
                            onPlaySong = { song, visibleList ->
                                val startIndex = visibleList.indexOf(song)
                                if (startIndex >= 0) {
                                    MusicController.playQueue(context, visibleList, startIndex)
                                    showPlayer = true
                                }
                            },
                            onToggleFavorite = { song -> toggleFavorite(song) },
                            onTogglePlay = { MusicController.toggle(context) },
                            onOpenPlayer = { showPlayer = true }
                        )
                    }
                }
            }
        }
    }
}
