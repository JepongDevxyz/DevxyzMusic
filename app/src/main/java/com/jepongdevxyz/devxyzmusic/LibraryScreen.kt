package com.jepongdevxyz.devxyzmusic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LibraryScreen(
    songs: List<Song>,
    favorites: Set<Long>,
    isPlaying: Boolean,
    currentSong: Song?,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onPickFiles: () -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onTogglePlay: () -> Unit,
    onOpenPlayer: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }

    val visibleSongs = if (tab == 1) {
        songs.filter { favorites.contains(it.id) }
    } else {
        songs
    }.filter { song ->
        if (query.isBlank()) {
            true
        } else {
            song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "DevxyzMusic", fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Local lang — walang streaming, walang login.",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Hanapin ang kanta o artist") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Button(onClick = { tab = 0 }) {
                Text("Songs (${songs.size})")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { tab = 1 }) {
                Text("Favorites (${songs.count { favorites.contains(it.id) }})")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (!hasPermission) {
            Button(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
                Text("Payagan ang audio access")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(onClick = onPickFiles, modifier = Modifier.fillMaxWidth()) {
            Text("Pumili ng audio file sa phone")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (visibleSongs.isEmpty()) {
            Text(
                text = if (songs.isEmpty()) {
                    "Walang kanta pa. Payagan ang audio access, o pumili ng audio file sa phone mo."
                } else {
                    "Walang tugma sa hinahanap mo."
                }
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visibleSongs) { song ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlaySong(song, visibleSongs) }
                            .padding(vertical = 8.dp)
                    ) {
                        AlbumArt(
                            song = song,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.artist,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(text = formatDuration(song.durationMs), fontSize = 13.sp)
                        IconButton(onClick = { onToggleFavorite(song) }) {
                            Icon(
                                imageVector = if (favorites.contains(song.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite"
                            )
                        }
                    }
                }
            }
        }

        if (currentSong != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenPlayer() }
                    .padding(8.dp)
            ) {
                AlbumArt(
                    song = currentSong,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onTogglePlay) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
            }
        }
    }
}
