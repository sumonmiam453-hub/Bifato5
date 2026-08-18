package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.R2StorageConfigEntity
import com.example.ui.theme.FacebookBlue
import com.example.util.R2StorageManager
import com.example.util.SoundManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    configs: List<R2StorageConfigEntity>,
    activeConfig: R2StorageConfigEntity?,
    onSaveConfig: (R2StorageConfigEntity) -> Unit,
    onSetActive: (Long) -> Unit,
    onDeleteConfig: (Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var configToEdit by remember { mutableStateOf<R2StorageConfigEntity?>(null) }
    var configToDelete by remember { mutableStateOf<R2StorageConfigEntity?>(null) }
    var testingConfigId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Storage Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                            SoundManager.playClickSound()
                            configToEdit = null
                            showAddEditDialog = true
                        },
                        modifier = Modifier.testTag("add_r2_storage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Cloudflare R2 Account",
                            tint = FacebookBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    SoundManager.playClickSound()
                    configToEdit = null
                    showAddEditDialog = true
                },
                containerColor = FacebookBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_r2_storage")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Storage Bucket")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FacebookBlue.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Cloudflare R2 Object Storage",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = FacebookBlue
                                )
                                Text(
                                    text = "S3-Compatible Zero-Egress Media Hosting",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Connect your Cloudflare R2 bucket to store compressed images (100–600 KB) and optimized video posts. You can configure multiple accounts and toggle the Active bucket with the Radio button.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compression logic badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Client Compression: Photos (100-600 KB) • Videos Optimized",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured R2 Accounts (${configs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (activeConfig != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active: ${activeConfig.label}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    } else {
                        Text(
                            text = "No Active Bucket",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE41E3F)
                        )
                    }
                }
            }

            // Empty State
            if (configs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Cloudflare R2 Account Added",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the button below to add your Cloudflare R2 credentials (Bucket, Access Key, Secret Key, Public Endpoint).",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    SoundManager.playClickSound()
                                    configToEdit = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Cloudflare R2 Bucket", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // List of R2 Accounts
                items(configs, key = { it.id }) { config ->
                    val isActive = config.id == activeConfig?.id || config.isActive
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isActive) {
                                    Modifier.border(1.5.dp, FacebookBlue, RoundedCornerShape(14.dp))
                                } else Modifier
                            ),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Radio Button + Label + Active Badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        SoundManager.playClickSound()
                                        onSetActive(config.id)
                                        Toast.makeText(context, "'${config.label}' is now Active Storage!", Toast.LENGTH_SHORT).show()
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isActive,
                                    onClick = {
                                        SoundManager.playClickSound()
                                        onSetActive(config.id)
                                        Toast.makeText(context, "'${config.label}' is now Active Storage!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = FacebookBlue,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = config.label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(FacebookBlue)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Bucket: ${config.bucketName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            // Detail Rows
                            DetailItem(icon = Icons.Default.Storage, label = "Account ID", value = config.accountId.take(12) + "...")
                            DetailItem(icon = Icons.Default.Key, label = "Access Key ID", value = config.accessKeyId.take(8) + "••••••••")
                            DetailItem(
                                icon = Icons.Default.Link,
                                label = "Public Endpoint",
                                value = config.publicEndpoint.ifBlank { "Direct R2 S3 URL" }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Test Connection Button
                                val isTesting = testingConfigId == config.id
                                OutlinedButton(
                                    onClick = {
                                        SoundManager.playClickSound()
                                        testingConfigId = config.id
                                        scope.launch {
                                            val result = R2StorageManager.testConnection(config)
                                            testingConfigId = null
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "✅ Connection Succeeded!\n${result.getOrNull()}", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "❌ Connection Failed:\n${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isTesting
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Testing...", fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp), tint = FacebookBlue)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Test", fontSize = 12.sp, color = FacebookBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Edit Button
                                OutlinedButton(
                                    onClick = {
                                        SoundManager.playClickSound()
                                        configToEdit = config
                                        showAddEditDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Delete Button
                                IconButton(
                                    onClick = {
                                        SoundManager.playClickSound()
                                        configToDelete = config
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE41E3F))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add / Edit R2 Configuration Dialog
    if (showAddEditDialog) {
        R2ConfigAddEditDialog(
            initialConfig = configToEdit,
            onSave = { newOrUpdated ->
                onSaveConfig(newOrUpdated)
                showAddEditDialog = false
                Toast.makeText(context, "Storage credentials saved! 🎉", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (configToDelete != null) {
        AlertDialog(
            onDismissRequest = { configToDelete = null },
            title = { Text("Delete Storage Account", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete the Cloudflare R2 credentials for '${configToDelete?.label}'?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        configToDelete?.let { onDeleteConfig(it.id) }
                        configToDelete = null
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE41E3F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { configToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = FacebookBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun R2ConfigAddEditDialog(
    initialConfig: R2StorageConfigEntity?,
    onSave: (R2StorageConfigEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf(initialConfig?.label ?: "Cloudflare R2 Storage") }
    var accountId by remember { mutableStateOf(initialConfig?.accountId ?: "") }
    var bucketName by remember { mutableStateOf(initialConfig?.bucketName ?: "") }
    var accessKeyId by remember { mutableStateOf(initialConfig?.accessKeyId ?: "") }
    var secretAccessKey by remember { mutableStateOf(initialConfig?.secretAccessKey ?: "") }
    var publicEndpoint by remember { mutableStateOf(initialConfig?.publicEndpoint ?: "") }
    var isActive by remember { mutableStateOf(initialConfig?.isActive ?: true) }
    var showSecret by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, contentDescription = null, tint = FacebookBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialConfig != null) "Edit R2 Credentials" else "Add Cloudflare R2 Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Fill in your Cloudflare R2 API credentials for media uploads:",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Account Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Account Label") },
                    placeholder = { Text("e.g. Production R2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Bucket Name
                OutlinedTextField(
                    value = bucketName,
                    onValueChange = { bucketName = it },
                    label = { Text("Bucket Name *") },
                    placeholder = { Text("e.g. bika-media") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Cloudflare Account ID
                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Cloudflare Account ID *") },
                    placeholder = { Text("e.g. abc123def456...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Access Key ID
                OutlinedTextField(
                    value = accessKeyId,
                    onValueChange = { accessKeyId = it },
                    label = { Text("Access Key ID *") },
                    placeholder = { Text("e.g. 78a9c24...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Secret Access Key
                OutlinedTextField(
                    value = secretAccessKey,
                    onValueChange = { secretAccessKey = it },
                    label = { Text("Secret Access Key *") },
                    placeholder = { Text("Enter secret key") },
                    singleLine = true,
                    visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showSecret = !showSecret }) {
                            Icon(
                                if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showSecret) "Hide Secret" else "Show Secret"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Public URL Endpoint
                OutlinedTextField(
                    value = publicEndpoint,
                    onValueChange = { publicEndpoint = it },
                    label = { Text("Public URL Endpoint (CDN/R2)") },
                    placeholder = { Text("https://pub-xxxx.r2.dev or cdn.domain.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. Make Active Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Set as Active Storage", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(checkedThumbColor = FacebookBlue)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bucketName.isBlank() || accountId.isBlank() || accessKeyId.isBlank() || secretAccessKey.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields marked with *", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val config = R2StorageConfigEntity(
                        id = initialConfig?.id ?: 0,
                        label = label.ifBlank { "R2 Bucket" },
                        bucketName = bucketName.trim(),
                        accountId = accountId.trim(),
                        accessKeyId = accessKeyId.trim(),
                        secretAccessKey = secretAccessKey.trim(),
                        publicEndpoint = publicEndpoint.trim(),
                        isActive = isActive
                    )
                    onSave(config)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Credentials", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
