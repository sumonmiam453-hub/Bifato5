package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

object MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingTrackId: String? = null
        private set

    fun playTrack(context: Context, trackId: String, url: String) {
        if (url.isBlank()) {
            stopTrack()
            return
        }
        stopTrack()
        try {
            val player = MediaPlayer()
            mediaPlayer = player
            currentlyPlayingTrackId = trackId

            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            player.setOnErrorListener { mp, what, extra ->
                Log.w("MusicPlayerManager", "MediaPlayer error suppressed: what=$what, extra=$extra")
                try {
                    mp.reset()
                    mp.release()
                } catch (_: Exception) {}
                if (mediaPlayer == mp) {
                    mediaPlayer = null
                    currentlyPlayingTrackId = null
                }
                true // Catch native error (1, -2147483648) gracefully
            }

            player.setOnCompletionListener {
                try {
                    if (it.isLooping) {
                        it.start()
                    }
                } catch (_: Exception) {}
            }

            player.setDataSource(url)
            player.setOnPreparedListener { mp ->
                try {
                    if (currentlyPlayingTrackId == trackId) {
                        mp.isLooping = true
                        mp.start()
                    } else {
                        mp.release()
                    }
                } catch (e: Exception) {
                    Log.w("MusicPlayerManager", "Failed to start playback on prepared", e)
                }
            }
            player.prepareAsync()
        } catch (e: Exception) {
            Log.w("MusicPlayerManager", "Error preparing track: $url", e)
            mediaPlayer = null
            currentlyPlayingTrackId = null
        }
    }

    fun stopTrack() {
        try {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                } catch (_: Exception) {}
                try {
                    mp.reset()
                    mp.release()
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            Log.w("MusicPlayerManager", "Error in stopTrack", e)
        } finally {
            mediaPlayer = null
            currentlyPlayingTrackId = null
        }
    }
}
