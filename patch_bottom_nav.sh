#!/bin/bash
cat << 'KOTLIN' > app/src/main/java/com/example/ui/components/FacebookBottomNav.kt
package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.FacebookBlue
import androidx.compose.foundation.border

@Composable
fun FacebookBottomNav(
    selectedTab: Int,
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
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Tab 0)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_home,
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            
            // Reels / Video (Tab 2)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_reels,
                label = "Reels",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            
            // Create Post (+)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .clickable { onCreatePostClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FacebookBlue),
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
                isSelected = false,
                onClick = { onMessengerClick() }
            )
            
            // Profile (Tab 3)
            BottomNavItemCustom(
                iconResId = R.drawable.ic_bold_profile,
                label = "Profile",
                isSelected = selectedTab == 3,
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
            .height(60.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label, 
                fontSize = 10.sp, 
                color = tint
            )
        }
    }
}
KOTLIN
