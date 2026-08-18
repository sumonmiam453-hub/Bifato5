package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

data class StoryMusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val url: String,
    val category: String = "Music"
)

val sampleStoryMusicTracks = listOf(
    StoryMusicTrack("1", "Moner Goohine (Music)", "Arijit Singh", "0:45", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Music"),
    StoryMusicTrack("2", "Hasbi Rabbi Jallallah (Ghazal)", "Qari Ahsan", "0:30", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Ghazal"),
    StoryMusicTrack("3", "Jiboner Sarthokota (Sermon)", "Mizanur Rahman Azhari", "0:50", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Sermon"),
    StoryMusicTrack("4", "O Priyotoma (Music)", "Balam & Somnur", "0:40", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "Music"),
    StoryMusicTrack("5", "Tawfeeq Dao Elahi (Ghazal)", "Kalarab Group", "0:35", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "Ghazal"),
    StoryMusicTrack("6", "Mayer Bhalobasha (Sermon)", "Abu Toha", "0:60", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "Sermon"),
    StoryMusicTrack("7", "Tumi Amar Shokkal (Music)", "Tahsan Khan", "0:50", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "Music"),
    StoryMusicTrack("8", "Allahu Allahu (Ghazal)", "Saimum Group", "0:40", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "Ghazal"),
    StoryMusicTrack("9", "Namajer Gurutwo (Sermon)", "Shaykh Ahmadullah", "0:55", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "Sermon"),
    StoryMusicTrack("10", "Bhalobashi Tomake (Music)", "Minar Rahman", "0:30", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3", "Music"),
    StoryMusicTrack("11", "Esho He Ramadan (Ghazal)", "Holy Tune", "0:50", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3", "Ghazal"),
    StoryMusicTrack("12", "Shona Bondhu Tui Amare (Music)", "Kuddus Boyati", "0:45", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3", "Music"),
    StoryMusicTrack("13", "Subhanallah (Ghazal)", "Iqra Group", "0:35", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3", "Ghazal"),
    StoryMusicTrack("14", "Somoy Khub Kormo (Sermon)", "Tarek Monowar", "0:40", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3", "Sermon"),
    StoryMusicTrack("15", "Amar Ekla Akash (Music)", "Anupam Roy", "0:60", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3", "Music")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStoryScreen(
    onDismiss: () -> Unit,
    onStoryCreated: (String, String?, String?) -> Unit, // imageUrl/color, text, musicTrack
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var storyText by remember { mutableStateOf("") }
    var selectedMusic by remember { mutableStateOf<StoryMusicTrack?>(null) }
    var isEditingStory by remember { mutableStateOf(false) }
    var showMusicPicker by remember { mutableStateOf(false) }
    var isTextStoryMode by remember { mutableStateOf(false) }

    val sampleGalleryPhotos = remember { emptyList<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = com.example.util.FileUtils.getLocalFilePathFromUri(context, it)
            isEditingStory = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        if (isEditingStory) {
            // Story Preview / Editor
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {
                // Canvas Image or Color Gradient Background
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Story preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))
                                )
                            )
                    )
                }

                // Overlay Controls Top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            isEditingStory = false
                            selectedImageUri = null
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Music Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedMusic != null) FacebookBlue else Color.Black.copy(alpha = 0.5f))
                                .clickable { showMusicPicker = true }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedMusic?.title ?: "Add Music",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Story Overlay Text Input Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = storyText,
                        onValueChange = { storyText = it },
                        placeholder = { Text("Type something...", color = Color.White.copy(alpha = 0.7f), fontSize = 22.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )

                    selectedMusic?.let { music ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("🎵 ${music.title} • ${music.artist}", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Bottom Share Button
                Button(
                    onClick = {
                        val finalImage = selectedImageUri ?: "https://images.unsplash.com/photo-1519681393784-d120267933ba?auto=format&fit=crop&w=600&q=80"
                        onStoryCreated(finalImage, storyText.ifBlank { null }, selectedMusic?.title)
                        SoundManager.playUploadSound()
                        Toast.makeText(context, "Story shared to BifaTo! 🌟", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp)
                        .align(Alignment.BottomCenter)
                        .testTag("share_story_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Share to Story", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            // Main Facebook "Create Story" Gallery & Action Page
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                    }

                    Text(
                        text = "Create story",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { Toast.makeText(context, "Story Settings", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    }
                }

                // Top 3 Quick Action Cards: Text, Music, Collage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Text Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                isTextStoryMode = true
                                isEditingStory = true
                            }
                            .testTag("story_action_text"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF8A2387), Color(0xFFE94057))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.TextFields, contentDescription = "Text", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Text", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // Music Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                showMusicPicker = true
                            }
                            .testTag("story_action_music"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Music", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // Collage Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clickable {
                                photoPickerLauncher.launch("image/*")
                            }
                            .testTag("story_action_collage"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF7971E), Color(0xFFFFD200))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.GridOn, contentDescription = "Collage", tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Collage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gallery Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gallery", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }

                    // Select Multiple Pill Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { photoPickerLauncher.launch("image/*") }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select multiple", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Gallery Grid with Floating Camera FAB
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(sampleGalleryPhotos) { photoUrl ->
                            Box(
                                modifier = Modifier
                                    .height(140.dp)
                                    .clickable {
                                        selectedImageUri = photoUrl
                                        isEditingStory = true
                                    }
                            ) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Gallery item",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Bottom Right Camera Floating Button
                    FloatingActionButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .testTag("story_camera_fab"),
                        containerColor = FacebookBlue,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        // Music Picker Bottom Sheet
        if (showMusicPicker) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showMusicPicker = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pick Background Music 🎵", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showMusicPicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sampleStoryMusicTracks) { track ->
                            val isSelected = selectedMusic?.id == track.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) FacebookBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .clickable {
                                        selectedMusic = track
                                        SoundManager.playClickSound()
                                        if (!isEditingStory) {
                                            isEditingStory = true
                                        }
                                        showMusicPicker = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(FacebookBlue.copy(alpha = 0.2f))
                                            .clickable {
                                                if (com.example.util.MusicPlayerManager.currentlyPlayingTrackId == track.id) {
                                                    com.example.util.MusicPlayerManager.stopTrack()
                                                } else {
                                                    com.example.util.MusicPlayerManager.playTrack(context, track.id, track.url)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (com.example.util.MusicPlayerManager.currentlyPlayingTrackId == track.id) Icons.Default.Stop else Icons.Default.PlayArrow, 
                                            contentDescription = null, 
                                            tint = FacebookBlue, 
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(track.title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                        Text("${track.artist} • ${track.duration}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = FacebookBlue)
                                } else {
                                    Icon(Icons.Default.MusicNote, contentDescription = "Music", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            com.example.util.MusicPlayerManager.stopTrack()
        }
    }
}
