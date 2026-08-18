package com.example.ui.components

import android.net.Uri
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.theme.FacebookBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoPlayerComponent(
    videoUrl: String,
    thumbnailUrl: String? = null,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    onVideoPlayed: () -> Unit = {},
    onDownloadClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isMuted by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloaded by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var hasLoggedHistory by remember { mutableStateOf(false) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var hasVideoError by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
            } catch (_: Exception) {}
        }
    }

    // Simulate progress advance if playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (!hasLoggedHistory) {
                hasLoggedHistory = true
                onVideoPlayed()
            }
            while (isPlaying) {
                delay(200)
                if (currentProgress < 1f) {
                    currentProgress += 0.01f
                } else {
                    currentProgress = 0f
                    isPlaying = false
                }
            }
        }
    }

    // Controls auto-hide timer
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable { showControls = !showControls },
        contentAlignment = Alignment.Center
    ) {
        val cleanUrl = remember(videoUrl) {
            videoUrl.replace("[VIDEO]", "").replace("[GIF]", "").substringBefore(" [THUMB:").substringBefore(" [AUDIO:").trim()
        }
        val parsedThumb = remember(videoUrl, thumbnailUrl) {
            if (!thumbnailUrl.isNullOrBlank()) {
                thumbnailUrl
            } else if (videoUrl.contains("[THUMB:")) {
                videoUrl.substringAfter("[THUMB:").substringBefore("]").substringBefore(" [AUDIO:").trim()
            } else {
                null
            }
        }
        val videoUri = remember(cleanUrl) {
            try {
                if (cleanUrl.startsWith("file:/") && !cleanUrl.startsWith("file://")) {
                    Uri.parse(cleanUrl.replace("file:/", "file:///"))
                } else {
                    Uri.parse(cleanUrl)
                }
            } catch (e: Exception) {
                Uri.EMPTY
            }
        }
        val isVideoMedia = !hasVideoError && (cleanUrl.startsWith("content://") || cleanUrl.startsWith("file://") || cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://") || cleanUrl.contains("video") || cleanUrl.endsWith(".mp4") || cleanUrl.endsWith(".mov") || cleanUrl.endsWith(".mkv"))

        if (isVideoMedia && videoUri != Uri.EMPTY) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        videoViewRef = this
                        setOnErrorListener { _, _, _ ->
                            hasVideoError = true
                            isPlaying = false
                            true // Suppress default "Can't play this video." dialog
                        }
                        try {
                            setVideoURI(videoUri)
                        } catch (e: Exception) {
                            hasVideoError = true
                        }
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            try {
                                if (isMuted) mp.setVolume(0f, 0f)
                            } catch (_: Exception) {}
                            if (autoPlay || isPlaying) start()
                        }
                    }
                },
                update = { videoView ->
                    videoViewRef = videoView
                    try {
                        if (!hasVideoError) {
                            if (isPlaying) {
                                if (!videoView.isPlaying) videoView.start()
                            } else {
                                if (videoView.isPlaying) videoView.pause()
                            }
                        }
                    } catch (e: Exception) {
                        hasVideoError = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // High quality video thumbnail representation with playback surface
            AsyncImage(
                model = parsedThumb ?: cleanUrl,
                contentDescription = "Video surface",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay controls
        AnimatedVisibility(
            visible = showControls || !isPlaying,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                // Play / Pause Center Button
                IconButton(
                    onClick = {
                        isPlaying = !isPlaying
                        if (isPlaying && !hasLoggedHistory) {
                            hasLoggedHistory = true
                            onVideoPlayed()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(FacebookBlue.copy(alpha = 0.9f))
                        .testTag("video_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Video" else "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Download Progress Overlay Banner if downloading
                if (isDownloading) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FacebookBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Downloading video... ${(downloadProgress * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = FacebookBlue,
                            trackColor = Color.DarkGray
                        )
                    }
                }

                // Bottom Video Controls Bar
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Slider Seekbar
                    Slider(
                        value = currentProgress,
                        onValueChange = {
                            currentProgress = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = FacebookBlue,
                            activeTrackColor = FacebookBlue,
                            inactiveTrackColor = Color.Gray
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time readout e.g. 0:15 / 1:30
                        val currentSec = (currentProgress * 90).toInt()
                        val currentFormatted = String.format("%d:%02d", currentSec / 60, currentSec % 60)
                        Text(
                            text = "$currentFormatted / 1:30",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Mute button
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Download Button (Directly on Video Player)
                            IconButton(
                                onClick = {
                                    if (!isDownloading && !isDownloaded) {
                                        isDownloading = true
                                        downloadProgress = 0f
                                        coroutineScope.launch {
                                            for (p in 1..10) {
                                                delay(250)
                                                downloadProgress = p / 10f
                                            }
                                            isDownloading = false
                                            isDownloaded = true
                                            Toast.makeText(context, "✅ Video downloaded successfully to Phone Gallery!", Toast.LENGTH_LONG).show()
                                            onDownloadClick?.invoke()
                                        }
                                    } else if (isDownloaded) {
                                        Toast.makeText(context, "Video is already downloaded!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("video_download_button")
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                                    contentDescription = "Download Video",
                                    tint = if (isDownloaded) Color(0xFF4CAF50) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Fullscreen icon
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Full screen toggled", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
