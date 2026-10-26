package com.jepongdevxyz.devxyzmusic

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AlbumArt(song: Song?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(song?.artUri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(song?.artUri) {
        bitmap = null
        val artUri = song?.artUri ?: return@LaunchedEffect
        bitmap = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(artUri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Album art",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.background(Color(0xFFE8DCC3))
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color(0xFF948875),
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
fun PlayerScreen(
    onClose: () -> Unit,
    onToggleFavorite: (Song) -> Unit
) {
    val context = LocalContext.current
    val queue by PlayerState.queue.collectAsState()
    val index by PlayerState.index.collectAsState()
    val isPlaying by PlayerState.isPlaying.collectAsState()
    val position by PlayerState.positionMs.collectAsState()
    val duration by PlayerState.durationMs.collectAsState()
    val shuffle by PlayerState.shuffle.collectAsState()
    val repeatMode by PlayerState.repeatMode.collectAsState()
    val favorites by PlayerState.favorites.collectAsState()
    val speed by PlayerState.speed.collectAsState()

    var showQueue by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    val song = queue.getOrNull(index)
    if (song == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(Color(0xFFE4D7C7))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Walang kantang naka-play", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onClose) { Text("Bumalik sa library") }
            }
        }
        return
    }

    val isFavorite = favorites.contains(song.id)
    val effectiveDuration = if (duration > 0) duration else song.durationMs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE4D7C7),
                        Color(0xFFC7BEB0),
                        Color(0xFFA9A49A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: down arrow, artist, queue — gaya ng screenshot.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(37.dp)
                        .background(Color(0x807F7566), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Bumalik sa library",
                        tint = Color.White
                    )
                }
                Text(
                    text = song.artist,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { showQueue = true },
                    modifier = Modifier
                        .size(37.dp)
                        .background(Color(0x807F7566), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        contentDescription = "Up next queue",
                        tint = Color.White
                    )
                }
            }

            // Album art: malaki, halos full width — gaya ng screenshot.
            AlbumArt(
                song = song,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                // Title pill: title + artist + share / queue / heart.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFF948875))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = {
                            try {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/*"
                                    putExtra(Intent.EXTRA_STREAM, song.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(share, "I-share ang kanta")
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Hindi ma-share ang file na ito",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "I-share ang kanta",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showQueue = true },
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QueueMusic,
                            contentDescription = "Up next queue",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onToggleFavorite(song) },
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Alisin sa favorites" else "Idagdag sa favorites",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress + oras.
                val sliderEnabled = effectiveDuration > 0
                Slider(
                    value = if (sliderEnabled) position.toFloat().coerceIn(0f, effectiveDuration.toFloat()) else 0f,
                    onValueChange = { newValue ->
                        PlayerState.positionMs.value = newValue.toLong()
                    },
                    onValueChangeFinished = {
                        MusicController.seek(context, PlayerState.positionMs.value)
                    },
                    valueRange = if (sliderEnabled) 0f..effectiveDuration.toFloat() else 0f..1f,
                    enabled = sliderEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.4f)
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = formatDuration(position), color = Color.White, fontSize = 13.sp)
                    Text(text = formatDuration(effectiveDuration), color = Color.White, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Controls: shuffle, prev, big play/pause, next, repeat.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { MusicController.toggleShuffle(context) }) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = if (shuffle) "Shuffle naka-on" else "Shuffle naka-off",
                            tint = if (shuffle) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(
                        onClick = { MusicController.previous(context) },
                        modifier = Modifier
                            .size(37.dp)
                            .background(Color(0x807F7566), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Nakaraang kanta",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { MusicController.toggle(context) },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF948875), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { MusicController.next(context) },
                        modifier = Modifier
                            .size(37.dp)
                            .background(Color(0x807F7566), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Susunod na kanta",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { MusicController.cycleRepeat(context) }) {
                        Icon(
                            imageVector = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                            contentDescription = when (repeatMode) {
                                RepeatMode.OFF -> "Repeat naka-off"
                                RepeatMode.ALL -> "Repeat all"
                                RepeatMode.ONE -> "Repeat one"
                            },
                            tint = if (repeatMode == RepeatMode.OFF) Color.White.copy(alpha = 0.6f) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom pill: library, queue, speed, info — lahat may tunay na handler.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .border(1.dp, Color(0xFF7F7566), RoundedCornerShape(32.dp))
                        .background(Color(0x337F7566))
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF7F7566), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Album,
                            contentDescription = "Bumalik sa library",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showQueue = true }) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Up next queue",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showSpeed = true }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Playback speed",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { showInfo = true }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Detalye ng kanta",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showQueue) {
        AlertDialog(
            onDismissRequest = { showQueue = false },
            title = { Text("Up next") },
            text = {
                if (queue.isEmpty()) {
                    Text("Walang queue.")
                } else {
                    LazyColumn(modifier = Modifier.height(360.dp)) {
                        itemsIndexed(queue) { queueIndex, queueSong ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                if (queueIndex == index) {
                                    Icon(
                                        imageVector = Icons.Filled.GraphicEq,
                                        contentDescription = "Kasalukuyang tumutugtog",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = queueSong.title,
                                        fontWeight = if (queueIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = queueSong.artist,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        MusicController.playQueue(context, queue, queueIndex)
                                        showQueue = false
                                    }
                                ) {
                                    Text("Play")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQueue = false }) { Text("Isara") }
            }
        )
    }

    if (showSpeed) {
        AlertDialog(
            onDismissRequest = { showSpeed = false },
            title = { Text("Playback speed") },
            text = {
                Column {
                    Text("Kasalukuyan: ${speed}x")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { value ->
                            TextButton(
                                onClick = {
                                    MusicController.setSpeed(context, value)
                                    showSpeed = false
                                }
                            ) {
                                Text("${value}x")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeed = false }) { Text("Isara") }
            }
        )
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(song.title) },
            text = {
                Column {
                    Text("Artist: ${song.artist}")
                    Text("Album: ${song.album}")
                    Text("Tagal: ${formatDuration(effectiveDuration)}")
                    Text("Pinagmulan: ${song.pathLabel}")
                    Text("Local lang — walang streaming, walang login.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) { Text("Isara") }
            }
        )
    }
}
