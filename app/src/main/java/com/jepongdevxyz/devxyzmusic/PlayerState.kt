package com.jepongdevxyz.devxyzmusic

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow

object PlayerState {
    val queue = MutableStateFlow<List<Song>>(emptyList())
    val index = MutableStateFlow(-1)
    val isPlaying = MutableStateFlow(false)
    val positionMs = MutableStateFlow(0L)
    val durationMs = MutableStateFlow(0L)
    val shuffle = MutableStateFlow(false)
    val repeatMode = MutableStateFlow(RepeatMode.OFF)
    val favorites = MutableStateFlow<Set<Long>>(emptySet())
    val speed = MutableStateFlow(1.0f)

    fun currentSong(): Song? {
        val list = queue.value
        val i = index.value
        return if (i in list.indices) list[i] else null
    }
}

object MusicController {
    fun playQueue(context: Context, songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty() || startIndex !in songs.indices) return
        PlayerState.queue.value = songs
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_PLAY_AT
            putExtra(MusicService.EXTRA_INDEX, startIndex)
        }
        context.startService(intent)
    }

    fun toggle(context: Context) {
        send(context, MusicService.ACTION_TOGGLE)
    }

    fun next(context: Context) {
        send(context, MusicService.ACTION_NEXT)
    }

    fun previous(context: Context) {
        send(context, MusicService.ACTION_PREV)
    }

    fun seek(context: Context, positionMs: Long) {
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SEEK
            putExtra(MusicService.EXTRA_POSITION, positionMs)
        }
        context.startService(intent)
    }

    fun setSpeed(context: Context, speed: Float) {
        PlayerState.speed.value = speed
        val intent = Intent(context, MusicService::class.java).apply {
            action = MusicService.ACTION_SPEED
            putExtra(MusicService.EXTRA_SPEED, speed)
        }
        context.startService(intent)
    }

    fun toggleShuffle(context: Context) {
        PlayerState.shuffle.value = !PlayerState.shuffle.value
        // Persist the toggle through the service so playback state stays in one place.
        send(context, MusicService.ACTION_TOGGLE_SHUFFLE)
    }

    fun cycleRepeat(context: Context) {
        val nextMode = when (PlayerState.repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        PlayerState.repeatMode.value = nextMode
        send(context, MusicService.ACTION_CYCLE_REPEAT)
    }

    private fun send(context: Context, action: String) {
        val intent = Intent(context, MusicService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }
}
