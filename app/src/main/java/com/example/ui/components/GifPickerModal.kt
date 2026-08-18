package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

data class GifItem(
    val id: Int,
    val title: String,
    val url: String,
    val category: String
)

val SAMPLE_GIFS = listOf(
    GifItem(1, "Wow Cat", "https://media.giphy.com/media/3o7TKsjN42gScuzs9a/giphy.gif", "Cat"),
    GifItem(2, "Minion Laugh", "https://media.giphy.com/media/l0HlHFRbmaZtBRhXG/giphy.gif", "Laugh"),
    GifItem(3, "Dancing Dog", "https://media.giphy.com/media/13G7mmm3N0vE20/giphy.gif", "Dance"),
    GifItem(4, "Homer Bush", "https://media.giphy.com/media/cJMmZA5SXgCo0/giphy.gif", "Funny"),
    GifItem(5, "Gatsby Cheers", "https://media.giphy.com/media/g9582DNuQppxC/giphy.gif", "Cheers"),
    GifItem(6, "Clapping Carell", "https://media.giphy.com/media/d2JJ1XmyK3793f3O/giphy.gif", "Clap"),
    GifItem(7, "Mind Blown", "https://media.giphy.com/media/26u4cqiYI30juCOGY/giphy.gif", "Mind Blown"),
    GifItem(8, "Thumbs Up", "https://media.giphy.com/media/l41K3o5TzMan5yIE8/giphy.gif", "Thumbs Up"),
    GifItem(9, "Dance Party", "https://media.giphy.com/media/3o6Zt8rGMqVfjaFFFi/giphy.gif", "Dance"),
    GifItem(10, "Heart Love", "https://media.giphy.com/media/xT9IgG5083K3W30v8Q/giphy.gif", "Love"),
    GifItem(11, "Shocked Face", "https://media.giphy.com/media/l3q2K12v7LgVW3VzG/giphy.gif", "Shocked"),
    GifItem(12, "Confused John", "https://media.giphy.com/media/3o7qDEq2bMbcbPRQ2c/giphy.gif", "Confused"),
    GifItem(13, "Facepalm", "https://media.giphy.com/media/14fnBD3yPO0EG4/giphy.gif", "Facepalm"),
    GifItem(14, "Popcorn Time", "https://media.giphy.com/media/l0AMJLnn71jx08mA8/giphy.gif", "Popcorn"),
    GifItem(15, "High Five", "https://media.giphy.com/media/26n6R505IsnvM61co/giphy.gif", "High Five"),
    GifItem(16, "Alien Dance", "https://media.giphy.com/media/3oEjI6SIIHBdCQX22A/giphy.gif", "Dance"),
    GifItem(17, "On Fire", "https://media.giphy.com/media/xT0xeJpnrWC4XWblEk/giphy.gif", "Fire"),
    GifItem(18, "Celebrating Win", "https://media.giphy.com/media/l2JdU587uE60O7Nte/giphy.gif", "Celebrate"),
    GifItem(19, "Winking Eye", "https://media.giphy.com/media/3o6ZtaO9BZHcOjmErm/giphy.gif", "Wink"),
    GifItem(20, "Sleeping Zzz", "https://media.giphy.com/media/3oKIPnAiaMCws8nOsE/giphy.gif", "Sleep"),
    GifItem(21, "Cool Shades", "https://media.giphy.com/media/3o7TKTDn976rzVgky4/giphy.gif", "Cool"),
    GifItem(22, "Thinking Hard", "https://media.giphy.com/media/26AHCgWksxgSmBCiQ/giphy.gif", "Think"),
    GifItem(23, "Salute Respect", "https://media.giphy.com/media/l1J3pT777D3xM0B32/giphy.gif", "Salute"),
    GifItem(24, "Nodding Yes", "https://media.giphy.com/media/xT1XGzgkBTit85PWS4/giphy.gif", "Yes"),
    GifItem(25, "Wave Hello", "https://media.giphy.com/media/3o7TKRnoP3f4Wf2P4s/giphy.gif", "Wave"),
    GifItem(26, "Coffee Morning", "https://media.giphy.com/media/l0HlBO7eyXzSZkJri/giphy.gif", "Coffee"),
    GifItem(27, "Crying Tears", "https://media.giphy.com/media/3o6Zt481isNVuQI1l6/giphy.gif", "Cry"),
    GifItem(28, "Cat Hug", "https://media.giphy.com/media/xT8qB7SrwvQA9O3GTu/giphy.gif", "Hug"),
    GifItem(29, "Happy Birthday", "https://media.giphy.com/media/26AHPxxnKS4hTBA1q/giphy.gif", "Birthday")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GifPickerModal(
    onGifSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredGifs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SAMPLE_GIFS
        } else {
            SAMPLE_GIFS.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gif,
                        contentDescription = "GIF",
                        tint = FacebookBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Text(
                        text = "Select a GIF (${SAMPLE_GIFS.size} Available)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search GIFs...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = FacebookBlue)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("gif_search_input"),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // GIF Grid (2 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredGifs, key = { it.id }) { gif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                SoundManager.playClickSound()
                                onGifSelect(gif.url)
                                onDismiss()
                            }
                            .testTag("gif_item_${gif.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = gif.url,
                                contentDescription = gif.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = gif.title,
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
