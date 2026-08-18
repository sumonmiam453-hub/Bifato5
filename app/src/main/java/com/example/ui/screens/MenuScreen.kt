package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

data class SavedAccount(
    val name: String,
    val email: String,
    val avatarRes: Int,
    val notificationBadge: Int = 0
)

data class MenuShortcut(
    val title: String,
    val subtitle: String = "",
    val icon: ImageVector,
    val iconBgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    userProfile: UserProfileEntity?,
    userPages: List<String> = emptyList(),
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onProfileClick: () -> Unit,
    onSavedPostsClick: () -> Unit,
    onSwitchAccount: (String, String) -> Unit, // email, name
    onLogoutClick: () -> Unit,
    onCreateGroupClick: () -> Unit = {},
    onCreatePageClick: () -> Unit = {},
    onOpenGroups: () -> Unit = {},
    onOpenPages: () -> Unit = {},
    onOpenFeeds: () -> Unit = {},
    onOpenWatch: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenEntityPage: (String) -> Unit = {},
    onOpenWatchHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenProfessionalDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isAccountSwitcherExpanded by remember { mutableStateOf(false) }
    var isHelpExpanded by remember { mutableStateOf(false) }
    var isSettingsExpanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {

    val currentName = userProfile?.name ?: "User"

    // Facebook Multi-Account Saved Profiles
    val savedAccounts = remember(userProfile, userPages) {
        val baseAccounts = mutableListOf<SavedAccount>()
        userProfile?.let { prof ->
            baseAccounts.add(
                SavedAccount(
                    prof.name,
                    "Main Profile",
                    R.drawable.img_user_avatar,
                    notificationBadge = 0
                )
            )
        }
        // Add pages as accounts so user can switch to them
        userPages.forEachIndexed { index, pageName ->
            baseAccounts.add(SavedAccount(pageName, "$pageName@page.com", R.drawable.img_post_photo1, notificationBadge = 0))
        }
        baseAccounts
    }

    // Modern Facebook Styled Menu Shortcuts
    val shortcuts = listOf(
        MenuShortcut("Dashboard", "Insights, tools & monetization", Icons.Default.Assessment, Color(0xFF10B981)),
        MenuShortcut("Wallet", "Deposit, Withdraw & Balance", Icons.Default.AccountBalanceWallet, Color(0xFF1877F2)),
        MenuShortcut("Feeds", "Most recent posts", Icons.Default.RssFeed, Color(0xFF1877F2)),
        MenuShortcut("Saved", "Posts & items", Icons.Default.Bookmark, Color(0xFF8C32E2)),
        MenuShortcut("Marketplace", "Buy & sell", Icons.Default.ShoppingBag, Color(0xFF00A389)),
        MenuShortcut("Watch History", "Watched videos & reels", Icons.Default.History, Color(0xFFE41E3F)),
        MenuShortcut("Groups", "Community posts", Icons.Default.Groups, Color(0xFF1877F2)),
        MenuShortcut("Watch / Reels", "Trending videos", Icons.Default.OndemandVideo, Color(0xFFF02849)),
        MenuShortcut("Pages", "Business & brands", Icons.Default.Flag, Color(0xFFF7B125)),
        MenuShortcut("Memories", "On this day", Icons.Default.History, Color(0xFF2ABBA7)),
        MenuShortcut("Events", "Local activities", Icons.Default.Event, Color(0xFFFA383E)),
        MenuShortcut("Advertisement", "Create & manage ads", Icons.Default.Campaign, Color(0xFFE91E63)),
        MenuShortcut("Settings", "Account & Privacy", Icons.Default.Settings, Color(0xFF65676B))
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header & Profile Account Switcher Section
        item(span = { GridItemSpan(2) }) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Menu",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(onClick = { Toast.makeText(context, "Search active", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = { onOpenSettings() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary Profile Card with Account Switcher Toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_profile_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProfileClick() }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentAvatar = userProfile?.avatarUrl ?: ""
                            if (currentAvatar.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = currentAvatar,
                                    contentDescription = currentName,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_user_avatar),
                                    contentDescription = currentName,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "See your profile",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Switch Account Dropdown Button (Facebook Multi-Account Switch)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        SoundManager.playClickSound()
                                        isAccountSwitcherExpanded = !isAccountSwitcherExpanded
                                    }
                                    .testTag("switch_account_dropdown_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAccountSwitcherExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Account",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        // EXPANDED ACCOUNT SWITCHER SECTION
                        AnimatedVisibility(
                            visible = isAccountSwitcherExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Accounts & Profiles",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue
                                    )
                                    Text(
                                        text = "Switch Account",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // List saved accounts
                                savedAccounts.forEach { acc ->
                                    val isCurrent = acc.name.equals(currentName, ignoreCase = true)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isCurrent) FacebookBlue.copy(alpha = 0.12f) else Color.Transparent)
                                            .clickable {
                                                if (!isCurrent) {
                                                    SoundManager.playUploadSound()
                                                    onSwitchAccount(acc.email, acc.name)
                                                    Toast.makeText(context, "Switched account to ${acc.name} 🔄", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(id = acc.avatarRes),
                                            contentDescription = acc.name,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = acc.name,
                                                fontSize = 15.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = acc.email,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Active",
                                                tint = FacebookBlue,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            if (acc.notificationBadge > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFE41E3F))
                                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "${acc.notificationBadge}",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Button(
                                                onClick = {
                                                    SoundManager.playUploadSound()
                                                    onSwitchAccount(acc.email, acc.name)
                                                    Toast.makeText(context, "Switched to ${acc.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                            ) {
                                                Text("Switch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Log into another account option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            SoundManager.playClickSound()
                                            onLogoutClick()
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Account",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Log into another account",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FacebookBlue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Creation Buttons (Create Group & Create Page)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Create Group Button
                    Button(
                        onClick = {
                            SoundManager.playClickSound()
                            onCreateGroupClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("menu_create_group_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Group", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Create Page Button
                    Button(
                        onClick = {
                            SoundManager.playClickSound()
                            onCreatePageClick()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("menu_create_page_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7B125)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Page", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "All Shortcuts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Facebook Authentic Shortcuts Grid Items
        items(shortcuts) { shortcut ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        SoundManager.playClickSound()
                        when (shortcut.title) {
                            "Dashboard", "Professional Dashboard" -> onOpenProfessionalDashboard()
                            "Wallet" -> onOpenWallet()
                            "Feeds" -> onOpenFeeds()
                            "Saved" -> onSavedPostsClick()
                            "Marketplace" -> onOpenMarketplace()
                            "Watch History" -> onOpenWatchHistory()
                            "Groups" -> onOpenGroups()
                            "Watch / Reels" -> onOpenWatch()
                            "Pages" -> onOpenPages()
                            "Memories", "Events", "Advertisement" -> onOpenEntityPage(shortcut.title)
                            "Settings" -> onOpenSettings()
                            else -> Toast.makeText(context, "Opening ${shortcut.title}...", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("menu_shortcut_${shortcut.title}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Colored Round Icon Container like Real Facebook App
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(shortcut.iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = shortcut.icon,
                            contentDescription = shortcut.title,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = shortcut.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (shortcut.subtitle.isNotBlank()) {
                        Text(
                            text = shortcut.subtitle,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Help, Settings & Privacy, and Log Out Button
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Accordion 1: Help & Support
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isHelpExpanded = !isHelpExpanded },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Help & support", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Icon(
                            imageVector = if (isHelpExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }

                if (isHelpExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("• Help Center", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenEntityPage("Help Center") })
                        Text("• Support Inbox", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenEntityPage("Support Inbox") })
                        Text("• Report a Problem", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenEntityPage("Report a Problem") })
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accordion 2: Settings & Privacy
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSettingsExpanded = !isSettingsExpanded },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Settings & privacy", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                }

                if (isSettingsExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Dark Mode",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dark Mode", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { onToggleDarkMode() },
                                colors = SwitchDefaults.colors(checkedThumbColor = FacebookBlue),
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }
                        Text("• Settings", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenSettings() })
                        Text("• Device permissions", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenEntityPage("Device Permissions") })
                        Text("• Language", fontSize = 14.sp, modifier = Modifier.padding(vertical = 6.dp).clickable { onOpenSettings() })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Log Out Button
                Button(
                    onClick = {
                        SoundManager.playClickSound()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("menu_logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
}
