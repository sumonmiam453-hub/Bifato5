package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsBottomSheet(
    sheetState: SheetState,
    userName: String,
    privacyStatus: String = "PUBLIC",
    onOpenAccountStatus: () -> Unit = {},
    onViewAsClick: () -> Unit = {},
    onOpenPages: () -> Unit = {},
    onOpenGroups: () -> Unit = {},
    onOpenMonetization: () -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenMarketplace: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileUrl = "https://facebook.com/${userName.lowercase().replace(" ", ".")}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("profile_settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Profile Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Account status & Privacy
            ProfileSettingsColorfulOption(
                icon = Icons.Default.Info,
                iconColor = Color.White,
                iconBgColor = Color(0xFF6366F1),
                title = "Account Status",
                subtitle = "Manage account standing & profile privacy (${privacyStatus.lowercase().replaceFirstChar { it.uppercase() }})",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenAccountStatus()
                }
            )

            // 2. View as
            ProfileSettingsColorfulOption(
                icon = Icons.Default.RemoveRedEye,
                iconColor = Color.White,
                iconBgColor = Color(0xFF0284C7),
                title = "View As",
                subtitle = "See what your profile looks like to the public",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onViewAsClick()
                }
            )

            // 3. Monetization (Direct to Professional Dashboard)
            ProfileSettingsColorfulOption(
                icon = Icons.Default.MonetizationOn,
                iconColor = Color.White,
                iconBgColor = Color(0xFF10B981),
                title = "Monetization",
                subtitle = "Track earnings, stars & monetization criteria",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenMonetization()
                }
            )

            // 4. My Pages
            ProfileSettingsColorfulOption(
                icon = Icons.Default.Flag,
                iconColor = Color.White,
                iconBgColor = Color(0xFFF59E0B),
                title = "My Pages",
                subtitle = "Manage your business & brand pages",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenPages()
                }
            )

            // 5. My Groups
            ProfileSettingsColorfulOption(
                icon = Icons.Default.Groups,
                iconColor = Color.White,
                iconBgColor = Color(0xFF1877F2),
                title = "My Groups",
                subtitle = "View and interact in your joined communities",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenGroups()
                }
            )

            // 6. Wallet
            ProfileSettingsColorfulOption(
                icon = Icons.Default.AccountBalanceWallet,
                iconColor = Color.White,
                iconBgColor = Color(0xFF8B5CF6),
                title = "Wallet",
                subtitle = "View balance, add funds & withdraw money",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenWallet()
                }
            )

            // 7. Marketplace
            ProfileSettingsColorfulOption(
                icon = Icons.Default.ShoppingBag,
                iconColor = Color.White,
                iconBgColor = Color(0xFF00A389),
                title = "Marketplace",
                subtitle = "Browse & list items for sale",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenMarketplace()
                }
            )

            // 8. Settings
            ProfileSettingsColorfulOption(
                icon = Icons.Default.Settings,
                iconColor = Color.White,
                iconBgColor = Color(0xFF64748B),
                title = "Settings",
                subtitle = "Preferences, security & notifications",
                onClick = {
                    SoundManager.playClickSound()
                    onDismiss()
                    onOpenSettings()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Profile Link Section
            Text(
                text = "Your Profile Link",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profileUrl,
                fontSize = 13.sp,
                color = FacebookBlue,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Facebook Profile Link", profileUrl)
                    clipboard.setPrimaryClip(clip)
                    SoundManager.playLikeSound()
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("copy_profile_link_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Link",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copy link to profile",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ProfileSettingsColorfulOption(
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
