package com.jepongdevxyz.devxyzmusic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Taupe = Color(0xFF8A7D6B)
private val DarkTaupe = Color(0xFF6B5D4F)
private val Ink = Color(0xFF2B2118)
private val BeigeCard = Color(0xFFE8DCC3)

@Composable
fun LibraryScreen(
    songs: List<Song>,
    favorites: Set<Long>,
    isPlaying: Boolean,
    currentSong: Song?,
    hasPermission: Boolean,
    isScanning: Boolean,
    onRequestPermission: () -> Unit,
    onPickFiles: () -> Unit,
    onRescan: () -> Unit,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF2E6CF), Color(0xFFD8C9B0), Color(0xFFA89A86))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(text = "DevxyzMusic", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(
                text = "Local lang — walang streaming, walang login.",
                fontSize = 14.sp,
                color = DarkTaupe
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Hanapin ang kanta o artist", color = DarkTaupe) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BeigeCard,
                    unfocusedContainerColor = BeigeCard,
                    focusedBorderColor = Taupe,
                    unfocusedBorderColor = Taupe
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row {
                TabPill(
                    label = "Songs (${songs.size})",
                    selected = tab == 0,
                    onClick = { tab = 0 }
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabPill(
                    label = "Favorites (${songs.count { favorites.contains(it.id) }})",
                    selected = tab == 1,
                    onClick = { tab = 1 }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (!hasPermission) {
                Text(
                    text = "Payagan ang audio access para awtomatikong lumabas dito ang mga kanta mo.",
                    fontSize = 14.sp,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(8.dp))
                TaupeButton(text = "Payagan ang audio access", onClick = onRequestPermission)
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                TaupeButton(text = "I-scan ulit ang mga kanta", onClick = onRescan)
                Spacer(modifier = Modifier.height(8.dp))
            }
            TaupeButton(text = "Pumili ng audio file sa phone", onClick = onPickFiles)
            Spacer(modifier = Modifier.height(12.dp))

            if (isScanning) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = DarkTaupe, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Naghahanap ng mga kanta mo…", color = Ink)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (visibleSongs.isEmpty() && !isScanning) {
                Text(
                    text = if (songs.isEmpty() && hasPermission) {
                        "Walang nakitang audio file. Pindutin ang I-scan ulit, o pumili ng audio file sa phone mo."
                    } else if (songs.isEmpty()) {
                        "Walang kanta pa. Payagan ang audio access para awtomatikong lumabas ang mga kanta mo."
                    } else {
                        "Walang tugma sa hinahanap mo."
                    },
                    color = Ink
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
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    fontSize = 13.sp,
                                    color = DarkTaupe,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(text = formatDuration(song.durationMs), fontSize = 13.sp, color = DarkTaupe)
                            IconButton(onClick = { onToggleFavorite(song) }) {
                                Icon(
                                    imageVector = if (favorites.contains(song.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (favorites.contains(song.id)) Color(0xFFB3261E) else DarkTaupe
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
                        .clip(RoundedCornerShape(32.dp))
                        .background(Taupe)
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
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong.artist,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onTogglePlay) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected) Taupe else Color.Transparent)
            .border(1.dp, Taupe, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else DarkTaupe,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TaupeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Taupe, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = text)
    }
}
