package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entities.PostEntity
import com.example.ui.components.PostItemCard
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

data class SearchedUserItem(
    val name: String,
    val avatarUrl: String,
    var isFriendRequestSent: Boolean = false
)

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    posts: List<PostEntity>,
    onReactionSelect: (Long, String) -> Unit,
    onCommentClick: (Long) -> Unit,
    onShareClick: (Long) -> Unit,
    onSaveToggle: (Long) -> Unit,
    onDeletePost: (Long) -> Unit,
    onVisitProfile: (String, String) -> Unit = { _, _ -> },
    onSendFriendRequest: (String, String) -> Unit = { _, _ -> },
    currentUserName: String = "Maruf Hossain"
) {
    val context = LocalContext.current
    val recentSearches = remember { mutableStateListOf<String>() }

    // Derive matching people from post authors + query
    val matchingPeople = remember(searchQuery, posts) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            emptyList()
        } else {
            val list = mutableListOf<SearchedUserItem>()
            
            // 1. Author matches from posts
            val matchedAuthors = posts.filter { it.authorName.contains(query, ignoreCase = true) }
                .map { it.authorName to it.authorAvatarUrl }
                .distinctBy { it.first }

            matchedAuthors.forEach { (name, avatar) ->
                list.add(SearchedUserItem(name = name, avatarUrl = avatar.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150" }))
            }

            // 2. If no exact match or if user typed a specific name/ID, add the searched name as a direct result!
            if (list.none { it.name.equals(query, ignoreCase = true) }) {
                list.add(
                    0,
                    SearchedUserItem(
                        name = query,
                        avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
                    )
                )
            }
            list
        }
    }

    // Filter matching posts
    val matchingPosts = remember(searchQuery, posts) {
        val query = searchQuery.trim()
        if (query.isBlank()) emptyList()
        else posts.filter {
            it.content.contains(query, ignoreCase = true) || it.authorName.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (searchQuery.isBlank()) {
            // Recent Searches View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Searches",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (recentSearches.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            color = FacebookBlue,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { recentSearches.clear() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (recentSearches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search people by name or search posts...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    recentSearches.forEach { search ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSearchQueryChange(search) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = search,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(onClick = { recentSearches.remove(search) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Search Results View (People + Posts)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                // Section 1: People / User IDs
                if (matchingPeople.isNotEmpty()) {
                    item {
                        Text(
                            text = "People",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(matchingPeople) { person ->
                        var isRequested by remember { mutableStateOf(person.isFriendRequestSent) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    if (!recentSearches.contains(searchQuery)) {
                                        recentSearches.add(0, searchQuery)
                                    }
                                    onVisitProfile(person.name, person.avatarUrl)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = person.avatarUrl,
                                    contentDescription = person.name,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "User Profile",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        SoundManager.playClickSound()
                                        if (isRequested) {
                                            isRequested = false
                                            Toast.makeText(context, "Friend request cancelled", Toast.LENGTH_SHORT).show()
                                        } else {
                                            isRequested = true
                                            onSendFriendRequest(person.name, person.avatarUrl)
                                            Toast.makeText(context, "Friend request sent to ${person.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRequested) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    if (!isRequested) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (isRequested) "Requested" else "Add Friend",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRequested) MaterialTheme.colorScheme.onSurface else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Posts
                item {
                    Text(
                        text = "Posts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                if (matchingPosts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts matching \"$searchQuery\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(matchingPosts) { post ->
                        PostItemCard(
                            post = post,
                            onReactionSelect = { reaction -> onReactionSelect(post.id, reaction) },
                            onCommentClick = { onCommentClick(post.id) },
                            onShareClick = { onShareClick(post.id) },
                            onSaveToggle = { onSaveToggle(post.id) },
                            onDeletePost = { onDeletePost(post.id) },
                            onVisitProfile = onVisitProfile,
                            currentUserName = currentUserName
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
