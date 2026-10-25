package com.jepongdevxyz.devxyzmusic

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns

object MusicRepository {

    fun loadFromMediaStore(context: Context): List<Song> {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        // BROAD scan on purpose: NO IS_MUSIC filter. Maraming music file sa
        // phone ang hindi naka-flag na IS_MUSIC (downloads, recordings, files
        // na nilipat lang), at yun ang dahilan kaya 0 ang lumalabas kahit may
        // kanta na. Lahat ng audio sa MediaStore ang kinukuha, TITLE ASC.
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        try {
            context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val title = if (titleCol >= 0) cursor.getString(titleCol) else null
                    val artist = if (artistCol >= 0) cursor.getString(artistCol) else null
                    val album = if (albumCol >= 0) cursor.getString(albumCol) else null
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    val songUri = ContentUris.withAppendedId(uri, id)
                    val artUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )
                    } else {
                        null
                    }
                    songs.add(
                        Song(
                            id = id,
                            title = title?.takeIf { it.isNotBlank() } ?: "Walang pamagat",
                            artist = artist?.takeIf { it.isNotBlank() } ?: "Hindi kilalang artist",
                            album = album?.takeIf { it.isNotBlank() } ?: "Hindi kilalang album",
                            durationMs = duration,
                            uri = songUri,
                            artUri = artUri,
                            pathLabel = "MediaStore ng phone"
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted yet; the UI shows the permission button.
        }
        return songs
    }

    fun songFromPickedFile(context: Context, fileUri: Uri): Song? {
        var displayName: String? = null
        try {
            context.contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameCol = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameCol >= 0) {
                    displayName = cursor.getString(nameCol)
                }
            }
        } catch (e: SecurityException) {
            return null
        }
        val rawName = displayName ?: "Piniling audio file"
        val title = rawName.substringBeforeLast('.', rawName)
        // Stable negative ids for picked files so they never collide with MediaStore ids.
        val stableId = -(fileUri.toString().hashCode().toLong())
        return Song(
            id = stableId,
            title = title,
            artist = "Local file",
            album = "Piniling files",
            durationMs = 0L,
            uri = fileUri,
            artUri = null,
            pathLabel = "Piniling file sa phone"
        )
    }
}

object FavoritesStore {
    private const val PREFS = "devxyzmusic_favs"
    private const val KEY = "ids"

    fun load(context: Context): Set<Long> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY, emptySet())?.mapNotNull { it.toLongOrNull() }?.toSet()
            ?: emptySet()
    }

    fun save(context: Context, ids: Set<Long>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY, ids.map { it.toString() }.toSet()).apply()
    }
}
