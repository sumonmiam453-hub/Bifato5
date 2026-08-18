package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FacebookBlue

@Composable
fun FacebookBottomNav(
    selectedTab: Int,
    isMessengerActive: Boolean = false,
    onTabSelected: (Int) -> Unit,
    onCreatePostClick: () -> Unit,
    onMessengerClick: () -> Unit,
    userAvatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 6.dp)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Tab 0)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_home,
                label = "Home",
                isSelected = !isMessengerActive && selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            // Reels / Video (Tab 2)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_reels,
                label = "Reels",
                isSelected = !isMessengerActive && selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            
            // Create Post (+) - Elevated Floating Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = (-14).dp)
                        .size(44.dp)
                        .shadow(elevation = 5.dp, shape = CircleShape)
                        .border(width = 2.5.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                        .clip(CircleShape)
                        .background(FacebookBlue)
                        .clickable { onCreatePostClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Chats (Messenger)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_chat,
                label = "Chats",
                isSelected = isMessengerActive,
                onClick = { onMessengerClick() }
            )
            
            // Account (Profile)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_custom_profile,
                label = "Account",
                isSelected = !isMessengerActive && selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavItemCustom(
    iconResId: Int = 0,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    useMaterialIcon: Boolean = false,
    materialIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val tint = if (isSelected) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .weight(1f)
            .height(58.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (useMaterialIcon && materialIcon != null) {
                Icon(
                    imageVector = materialIcon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label, 
                fontSize = 11.sp, 
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tint
            )
        }
    }
}
