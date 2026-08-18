package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.PostEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalDashboardScreen(
    userProfile: UserProfileEntity?,
    userPosts: List<PostEntity>,
    walletBalance: Double,
    onBackClick: () -> Unit,
    onCreatePostClick: () -> Unit = {},
    onClaimCreatorFund: (Double) -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onToggleCreatorMode: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    // Handle System & Hardware Back Button
    BackHandler {
        onBackClick()
    }

    val userName = userProfile?.name ?: "Maruf Hossain"
    val followerCount = userProfile?.followerCount ?: 0

    // Filter user's posts
    val myPosts = remember(userPosts, userName) {
        userPosts.filter { post ->
            val author = post.authorName.trim()
            author.equals(userName, ignoreCase = true) ||
                    author.equals("Maruf Hossain", ignoreCase = true) ||
                    author.equals("Manik Hossain", ignoreCase = true) ||
                    author.equals("Me", ignoreCase = true) ||
                    author.equals("You", ignoreCase = true) ||
                    (userName.isNotBlank() && author.contains(userName, ignoreCase = true))
        }
    }

    // 1. Separate Video Reels vs Regular Posts (Image / Caption)
    val reelsPosts = remember(myPosts) {
        myPosts.filter { isReelVideoPost(it.imageUrl, it.content) }
    }
    val regularPosts = remember(myPosts) {
        myPosts.filter { !isReelVideoPost(it.imageUrl, it.content) }
    }

    val reelsCount = reelsPosts.size
    val postsCount = regularPosts.size

    // Total Views (Sum of views and interactions)
    val totalViews = remember(myPosts) {
        if (myPosts.isEmpty()) 0
        else myPosts.sumOf { post ->
            (post.sharesCount * 14 + post.likesCount * 4 + post.commentsCount * 2 + 12).coerceAtLeast(1)
        }
    }

    // Total Interactions (likes + comments + shares)
    val totalInteractions = remember(myPosts) {
        myPosts.sumOf { it.likesCount + it.commentsCount + it.sharesCount }
    }

    // Account Age: Simulated from account creation date (default 5 days)
    val accountAgeDays = 5

    // Criteria values requested by user:
    // 500 followers, 15 posts (captions & image), 10 reels videos, 3 days account age
    val requiredFollowers = 500
    val requiredPosts = 15
    val requiredReels = 10
    val requiredAccountDays = 3

    val isFollowersMet = followerCount >= requiredFollowers
    val isPostsMet = postsCount >= requiredPosts
    val isReelsMet = reelsCount >= requiredReels
    val isAgeMet = accountAgeDays >= requiredAccountDays

    val isAllCriteriaMet = isFollowersMet && isPostsMet && isReelsMet && isAgeMet

    var showApplySuccessDialog by remember { mutableStateOf(false) }
    var claimedRewardAmount by remember { mutableDoubleStateOf(0.0) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                delay(600)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Professional Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("dashboard_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isRefreshing = true
                                    delay(400)
                                    isRefreshing = false
                                    Toast.makeText(context, "Dashboard Refreshed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = FacebookBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF0F2F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ==========================================
                // SECTION 1: PERFORMANCE OVERVIEW (2x2 CARDS)
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Performance Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )

                    // Row 1: Followers & Total Views
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PerformanceMetricCard(
                            title = "Followers",
                            value = formatDisplayCount(followerCount),
                            icon = Icons.Default.People,
                            iconTint = Color(0xFF1877F2),
                            iconBackground = Color(0xFFE8F1FD),
                            modifier = Modifier.weight(1f)
                        )

                        PerformanceMetricCard(
                            title = "Total Views",
                            value = formatDisplayCount(totalViews),
                            icon = Icons.Default.Visibility,
                            iconTint = Color(0xFF9333EA),
                            iconBackground = Color(0xFFF3E8FD),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Posts & Reels (Replaces Scrolle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PerformanceMetricCard(
                            title = "Posts",
                            value = "$postsCount",
                            icon = Icons.Default.Article,
                            iconTint = Color(0xFF00A389),
                            iconBackground = Color(0xFFE6F7F3),
                            modifier = Modifier.weight(1f)
                        )

                        PerformanceMetricCard(
                            title = "Reels",
                            value = "$reelsCount",
                            icon = Icons.Default.PlayArrow,
                            iconTint = Color(0xFFEF4444),
                            iconBackground = Color(0xFFFEECEE),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==========================================
                // SECTION 2: MONETIZATION REQUIREMENTS CARD
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Monetization",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            // Header with $ Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = "Monetization",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "Monetization Requirements",
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1C1E21)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Earn 55% revenue share from ad views on your content",
                                fontSize = 13.sp,
                                color = Color(0xFF65676B),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 1. Followers Requirement (500)
                            MonetizationRequirementRow(
                                icon = Icons.Default.People,
                                title = "Followers",
                                progressText = "$followerCount / $requiredFollowers",
                                isCompleted = isFollowersMet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2. Posts Requirement (15 Captions / Image posts)
                            MonetizationRequirementRow(
                                icon = Icons.Default.Article,
                                title = "Posts",
                                progressText = "$postsCount / $requiredPosts",
                                isCompleted = isPostsMet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Reels Requirement (10 Video reels)
                            MonetizationRequirementRow(
                                icon = Icons.Default.PlayArrow,
                                title = "Reels",
                                progressText = "$reelsCount / $requiredReels",
                                isCompleted = isReelsMet
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. Account Age Requirement (3 Days)
                            MonetizationRequirementRow(
                                icon = Icons.Default.CalendarMonth,
                                title = "Account Age",
                                progressText = "$accountAgeDays / $requiredAccountDays days",
                                isCompleted = isAgeMet
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Bottom Action Button
                            if (isAllCriteriaMet) {
                                Button(
                                    onClick = {
                                        SoundManager.playUploadSound()
                                        claimedRewardAmount = 500.00
                                        onClaimCreatorFund(claimedRewardAmount)
                                        showApplySuccessDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("apply_monetization_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Apply for Monetization",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "Please complete all requirements to unlock monetization.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE4E6EB),
                                        disabledContainerColor = Color(0xFFE4E6EB)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("monetization_not_met_button")
                                ) {
                                    Text(
                                        text = "Requirements not met yet",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF8A8D91)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SECTION 3: THIS MONTH (REACH GRAPH / GROWTH CHART)
                // ==========================================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This Month",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )

                    AccountReachGrowthCard(
                        totalReach = totalViews,
                        totalInteractions = totalInteractions,
                        postsCount = myPosts.size
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Success Dialog on Monetization Approval
    if (showApplySuccessDialog) {
        AlertDialog(
            onDismissRequest = { showApplySuccessDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Monetization Approved!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Congratulations! Your profile has met all monetization criteria. Ad revenue sharing is now active.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bonus $$claimedRewardAmount has been added to your Wallet balance!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showApplySuccessDialog = false
                        onOpenWallet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text("Open Wallet", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showApplySuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Done", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Value & Title
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1E21)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color(0xFF65676B)
                )
            }
        }
    }
}

@Composable
private fun MonetizationRequirementRow(
    icon: ImageVector,
    title: String,
    progressText: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Light yellow/cream rounded box icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFEF3C7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and progress
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1E21)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = progressText,
                fontSize = 13.sp,
                color = Color(0xFF65676B)
            )
        }

        // Circular Status Indicator (Green Check or Gray Ring)
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFBEC3C9), CircleShape)
            )
        }
    }
}

@Composable
private fun AccountReachGrowthCard(
    totalReach: Int,
    totalInteractions: Int,
    postsCount: Int
) {
    val isReachUp = totalReach > 0 || totalInteractions > 0
    val growthPercentage = if (isReachUp) "+24.8%" else "-4.2%"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with Reach Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Account Reach & Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )
                    Text(
                        text = "Performance activity over the last 30 days",
                        fontSize = 12.sp,
                        color = Color(0xFF65676B)
                    )
                }

                // Growth Badge (Up or Down indicator)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isReachUp) Color(0xFFE6F7F3) else Color(0xFFFEECEE))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isReachUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = if (isReachUp) "Reach Up" else "Reach Down",
                        tint = if (isReachUp) Color(0xFF00A389) else Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isReachUp) "Reach Up $growthPercentage" else "Reach Down $growthPercentage",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReachUp) Color(0xFF00A389) else Color(0xFFEF4444)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7F8FA), RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Impressions", fontSize = 11.5.sp, color = Color(0xFF65676B))
                    Text(
                        text = formatDisplayCount(totalReach + 450),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color(0xFFE4E6EB))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Engaged", fontSize = 11.5.sp, color = Color(0xFF65676B))
                    Text(
                        text = formatDisplayCount(totalInteractions + 65),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1E21)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Color(0xFFE4E6EB))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Activity", fontSize = 11.5.sp, color = Color(0xFF65676B))
                    Text(
                        text = "$postsCount Posts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val lineColor = if (isReachUp) Color(0xFF1877F2) else Color(0xFFEF4444)
                val fillGradient = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Dashed background grid lines
                    val gridY1 = h * 0.25f
                    val gridY2 = h * 0.5f
                    val gridY3 = h * 0.75f

                    drawLine(
                        color = Color(0xFFE4E6EB),
                        start = Offset(0f, gridY1),
                        end = Offset(w, gridY1),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0xFFE4E6EB),
                        start = Offset(0f, gridY2),
                        end = Offset(w, gridY2),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0xFFE4E6EB),
                        start = Offset(0f, gridY3),
                        end = Offset(w, gridY3),
                        strokeWidth = 1f
                    )

                    // Data points for smooth curve
                    val points = listOf(
                        Offset(0f, h * 0.85f),
                        Offset(w * 0.15f, h * 0.75f),
                        Offset(w * 0.30f, h * 0.60f),
                        Offset(w * 0.45f, h * 0.70f),
                        Offset(w * 0.60f, h * 0.45f),
                        Offset(w * 0.75f, h * 0.30f),
                        Offset(w * 0.90f, h * 0.20f),
                        Offset(w, h * 0.15f)
                    )

                    val path = Path()
                    val fillPath = Path()

                    path.moveTo(points[0].x, points[0].y)
                    fillPath.moveTo(points[0].x, points[0].y)

                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cx = (prev.x + curr.x) / 2
                        path.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                        fillPath.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                    }

                    fillPath.lineTo(w, h)
                    fillPath.lineTo(0f, h)
                    fillPath.close()

                    // Draw gradient area under the curve
                    drawPath(fillPath, brush = fillGradient)

                    // Draw line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw latest point dot with halo
                    val lastPoint = points.last()
                    drawCircle(
                        color = lineColor.copy(alpha = 0.25f),
                        radius = 8.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 4.5.dp.toPx(),
                        center = lastPoint
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = lastPoint
                    )
                }
            }

            // Days labels on X-Axis
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Day 1", fontSize = 11.sp, color = Color(0xFF8A8D91))
                Text("Day 7", fontSize = 11.sp, color = Color(0xFF8A8D91))
                Text("Day 14", fontSize = 11.sp, color = Color(0xFF8A8D91))
                Text("Day 21", fontSize = 11.sp, color = Color(0xFF8A8D91))
                Text("Day 30", fontSize = 11.sp, color = Color(0xFF8A8D91))
            }
        }
    }
}

private fun formatDisplayCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
