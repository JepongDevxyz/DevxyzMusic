package com.jepongdevxyz.devxyzmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MusicService : Service() {

    companion object {
        const val ACTION_PLAY_AT = "com.jepongdevxyz.devxyzmusic.PLAY_AT"
        const val ACTION_TOGGLE = "com.jepongdevxyz.devxyzmusic.TOGGLE"
        const val ACTION_NEXT = "com.jepongdevxyz.devxyzmusic.NEXT"
        const val ACTION_PREV = "com.jepongdevxyz.devxyzmusic.PREV"
        const val ACTION_SEEK = "com.jepongdevxyz.devxyzmusic.SEEK"
        const val ACTION_SPEED = "com.jepongdevxyz.devxyzmusic.SPEED"
        const val ACTION_TOGGLE_SHUFFLE = "com.jepongdevxyz.devxyzmusic.TOGGLE_SHUFFLE"
        const val ACTION_CYCLE_REPEAT = "com.jepongdevxyz.devxyzmusic.CYCLE_REPEAT"
        const val EXTRA_INDEX = "index"
        const val EXTRA_POSITION = "position"
        const val EXTRA_SPEED = "speed"
        const val CHANNEL_ID = "now_playing"
        const val NOTIFICATION_ID = 1
    }

    private var player: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startTicker()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_AT -> {
                val index = intent.getIntExtra(EXTRA_INDEX, 0)
                playAt(index)
            }
            ACTION_TOGGLE -> toggle()
            ACTION_NEXT -> next()
            ACTION_PREV -> previous()
            ACTION_SEEK -> {
                val pos = intent.getLongExtra(EXTRA_POSITION, 0L)
                seekTo(pos)
            }
            ACTION_SPEED -> {
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)
                applySpeed(speed)
            }
            ACTION_TOGGLE_SHUFFLE -> {
                // PlayerState.shuffle is already toggled by MusicController.
            }
            ACTION_CYCLE_REPEAT -> {
                // PlayerState.repeatMode is already cycled by MusicController.
                applyRepeatToPlayer()
            }
        }
        updateNotification()
        return START_STICKY
    }

    private fun playAt(index: Int) {
        val queue = PlayerState.queue.value
        if (index !in queue.indices) return
        val song = queue[index]
        try {
            player?.release()
            val newPlayer = MediaPlayer()
            newPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            newPlayer.setDataSource(applicationContext, song.uri)
            newPlayer.setOnPreparedListener {
                applyRepeatToPlayer()
                applySpeed(PlayerState.speed.value)
                it.start()
                PlayerState.isPlaying.value = true
                PlayerState.durationMs.value = it.duration.toLong()
                updateNotification()
            }
            newPlayer.setOnCompletionListener {
                onTrackFinished()
            }
            newPlayer.setOnErrorListener { _, _, _ ->
                PlayerState.isPlaying.value = false
                Toast.makeText(
                    applicationContext,
                    "Hindi ma-play ang file na ito sa phone",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
            player = newPlayer
            PlayerState.index.value = index
            PlayerState.positionMs.value = 0L
            PlayerState.durationMs.value = song.durationMs
            newPlayer.prepareAsync()
        } catch (e: Exception) {
            PlayerState.isPlaying.value = false
            Toast.makeText(
                applicationContext,
                "Hindi ma-play ang file na ito sa phone",
                Toast.LENGTH_SHORT
            ).show()
        }
        updateNotification()
    }

    private fun toggle() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
            PlayerState.isPlaying.value = false
        } else {
            p.start()
            PlayerState.isPlaying.value = true
        }
        updateNotification()
    }

    private fun next() {
        val queue = PlayerState.queue.value
        if (queue.isEmpty()) return
        val current = PlayerState.index.value
        val nextIndex = if (PlayerState.shuffle.value && queue.size > 1) {
            var candidate = Random.nextInt(queue.size)
            while (candidate == current) {
                candidate = Random.nextInt(queue.size)
            }
            candidate
        } else if (current + 1 < queue.size) {
            current + 1
        } else if (PlayerState.repeatMode.value == RepeatMode.ALL) {
            0
        } else {
            return
        }
        playAt(nextIndex)
    }

    private fun previous() {
        val p = player
        if (p != null && p.isPlaying && p.currentPosition > 3000) {
            p.seekTo(0)
            PlayerState.positionMs.value = 0L
            return
        }
        val queue = PlayerState.queue.value
        if (queue.isEmpty()) return
        val current = PlayerState.index.value
        val prevIndex = if (current - 1 >= 0) {
            current - 1
        } else if (PlayerState.repeatMode.value == RepeatMode.ALL) {
            queue.size - 1
        } else {
            0
        }
        playAt(prevIndex)
    }

    private fun seekTo(positionMs: Long) {
        try {
            player?.seekTo(positionMs.toInt())
            PlayerState.positionMs.value = positionMs
        } catch (e: Exception) {
            // Player not ready yet; ignore the seek.
        }
    }

    private fun applySpeed(speed: Float) {
        try {
            val p = player ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                p.playbackParams = PlaybackParams().setSpeed(speed)
            }
        } catch (e: Exception) {
            // Some files do not support speed change; keep playing at normal speed.
        }
    }

    private fun applyRepeatToPlayer() {
        try {
            player?.isLooping = PlayerState.repeatMode.value == RepeatMode.ONE
        } catch (e: Exception) {
            // Ignore; repeat-one is re-applied on the next prepared track.
        }
    }

    private fun onTrackFinished() {
        if (PlayerState.repeatMode.value == RepeatMode.ONE) {
            return
        }
        val queue = PlayerState.queue.value
        val current = PlayerState.index.value
        if (current + 1 < queue.size || PlayerState.repeatMode.value == RepeatMode.ALL) {
            next()
        } else {
            PlayerState.isPlaying.value = false
            PlayerState.positionMs.value = 0L
            updateNotification()
        }
    }

    private fun startTicker() {
        if (tickerStarted) return
        tickerStarted = true
        scope.launch {
            while (isActive) {
                delay(500)
                val p = player
                if (p != null) {
                    try {
                        if (p.isPlaying) {
                            PlayerState.positionMs.value = p.currentPosition.toLong()
                            PlayerState.durationMs.value = p.duration.toLong()
                        }
                    } catch (e: Exception) {
                        // Player released mid-tick; the next tick reads the new player.
                    }
                }
            }
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.now_playing_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun actionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(): Notification {
        val song = PlayerState.currentSong()
        val isPlaying = PlayerState.isPlaying.value
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(song?.title ?: "DevxyzMusic")
            .setContentText(song?.artist ?: "Local music — galing sa phone mo")
            .setContentIntent(openIntent)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_music_note, "Prev", actionIntent(ACTION_PREV))
            .addAction(
                R.drawable.ic_music_note,
                if (isPlaying) "Pause" else "Play",
                actionIntent(ACTION_TOGGLE)
            )
            .addAction(R.drawable.ic_music_note, "Next", actionIntent(ACTION_NEXT))
            .build()
    }

    private fun updateNotification() {
        try {
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, buildNotification())
        } catch (e: Exception) {
            // Notification update is best-effort; playback continues.
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        try {
            player?.release()
        } catch (e: Exception) {
            // Already released.
        }
        player = null
    }
}
