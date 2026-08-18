package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.FirebaseManager
import com.example.data.UserProfile
import com.example.ui.theme.FacebookBlue
import com.example.util.SoundManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRegistering by remember { mutableStateOf(false) }

    // LOGIN STATE
    var loginEmailOrPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // REGISTRATION STATE
    var regStep by remember { mutableIntStateOf(1) } // 1: Details & Password, 2: Optional Photos
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var contactType by remember { mutableStateOf("Email") } // "Email" or "Phone"
    var contactValue by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Male") } // "Male", "Female", "Other"
    var regPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Image Upload State
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }

    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        avatarUri = uri
    }

    val coverLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverUri = uri
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Frndom Logo",
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("auth_logo")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Frndom",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = FacebookBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Connect with friends and the world around you",
                fontSize = 13.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isRegistering) {
                // ==================== LOGIN FORM ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = loginEmailOrPhone,
                        onValueChange = { loginEmailOrPhone = it },
                        placeholder = { Text("Mobile number or email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = FacebookBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        placeholder = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = FacebookBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (loginEmailOrPhone.isBlank() || loginPassword.isBlank()) {
                                Toast.makeText(context, "Please enter email/phone and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            SoundManager.playLikeSound()
                            coroutineScope.launch {
                                val res = FirebaseManager.loginWithEmail(loginEmailOrPhone.trim(), loginPassword)
                                isLoading = false
                                res.onSuccess { profile ->
                                    Toast.makeText(context, "Welcome back, ${profile.name}!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(profile)
                                }.onFailure { err ->
                                    Toast.makeText(context, err.message ?: "Login failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Forgot password?",
                        color = FacebookBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Please enter registered email to recover password", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Create Account Button
                Button(
                    onClick = {
                        SoundManager.playClickSound()
                        isRegistering = true
                        regStep = 1
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = FacebookBlue
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FacebookBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("create_new_account_button")
                ) {
                    Text("Create new account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // ==================== STREAMLINED REGISTRATION FORM ====================
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Back Arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            if (regStep > 1) {
                                regStep--
                            } else {
                                isRegistering = false
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = if (regStep == 1) "Create Account" else "Add Photos (Optional)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FacebookBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (regStep == 1) {
                        // STEP 1: All in one simple, clear form
                        Text(
                            text = "Join Frndom",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fill in your basic information to get started.",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 1. First Name (Left) & Last Name (Right) in a Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("First name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("register_first_name"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Last name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("register_last_name"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Email Address or Phone Number Selection
                        Text(
                            text = "Contact Information",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Email Option Pill
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (contactType == "Email") FacebookBlue.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { contactType = "Email" }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = contactType == "Email",
                                    onClick = { contactType = "Email" },
                                    colors = RadioButtonDefaults.colors(selectedColor = FacebookBlue)
                                )
                                Text("Email", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }

                            // Phone Option Pill
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (contactType == "Phone") FacebookBlue.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .clickable { contactType = "Phone" }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = contactType == "Phone",
                                    onClick = { contactType = "Phone" },
                                    colors = RadioButtonDefaults.colors(selectedColor = FacebookBlue)
                                )
                                Text("Phone", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic Input Field for Email or Phone
                        OutlinedTextField(
                            value = contactValue,
                            onValueChange = { contactValue = it },
                            label = { Text(if (contactType == "Email") "Email address" else "Phone number") },
                            placeholder = { Text(if (contactType == "Email") "example@domain.com" else "+8801XXXXXXXXX") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (contactType == "Email") Icons.Default.Email else Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = FacebookBlue
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_contact_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Gender Selector
                        Text(
                            text = "Gender",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Other").forEach { genderOption ->
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (selectedGender == genderOption) FacebookBlue.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .clickable { selectedGender = genderOption }
                                        .padding(horizontal = 4.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedGender == genderOption,
                                        onClick = { selectedGender = genderOption },
                                        colors = RadioButtonDefaults.colors(selectedColor = FacebookBlue)
                                    )
                                    Text(genderOption, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4. Password Field
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password (min 6 characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FacebookBlue) },
                            trailingIcon = {
                                IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isRegPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5. Confirm Password Field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FacebookBlue) },
                            trailingIcon = {
                                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_confirm_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Next Button
                        Button(
                            onClick = {
                                if (firstName.isBlank() || lastName.isBlank()) {
                                    Toast.makeText(context, "Please enter first and last name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (contactValue.isBlank()) {
                                    Toast.makeText(context, "Please enter your $contactType", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (regPassword.length < 6) {
                                    Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (regPassword != confirmPassword) {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                SoundManager.playClickSound()
                                regStep = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_step1_next_button")
                        ) {
                            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // STEP 2: Profile Picture & Banner Image (Optional with Skip)
                        Text(
                            text = "Add Profile & Banner Photos",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Upload custom photos or skip to use default avatars.",
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Profile Picture Selector
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { avatarLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUri != null) {
                                AsyncImage(
                                    model = avatarUri,
                                    contentDescription = "Selected Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Profile Photo", tint = FacebookBlue)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Profile Photo", fontSize = 11.sp, color = FacebookBlue, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Cover / Banner Selector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { coverLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (coverUri != null) {
                                AsyncImage(
                                    model = coverUri,
                                    contentDescription = "Selected Cover",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add Banner", tint = FacebookBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Cover / Banner Photo", fontSize = 13.sp, color = FacebookBlue, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Complete Registration Button
                        Button(
                            onClick = {
                                isLoading = true
                                SoundManager.playUploadSound()
                                coroutineScope.launch {
                                    val customAvatarStr = avatarUri?.let { FirebaseManager.uploadMedia(it.toString()) }
                                    val customCoverStr = coverUri?.let { FirebaseManager.uploadMedia(it.toString()) }

                                    val res = FirebaseManager.signUpWithFullDetails(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        emailOrPhone = contactValue.trim(),
                                        dob = "01 Jan 2000",
                                        gender = selectedGender,
                                        pass = regPassword,
                                        customAvatarUrl = customAvatarStr,
                                        customCoverUrl = customCoverStr
                                    )
                                    isLoading = false
                                    res.onSuccess { profile ->
                                        Toast.makeText(context, "Account successfully created!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(profile)
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_complete_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Skip Button
                        Button(
                            onClick = {
                                isLoading = true
                                coroutineScope.launch {
                                    val res = FirebaseManager.signUpWithFullDetails(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        emailOrPhone = contactValue.trim(),
                                        dob = "01 Jan 2000",
                                        gender = selectedGender,
                                        pass = regPassword,
                                        customAvatarUrl = null,
                                        customCoverUrl = null
                                    )
                                    isLoading = false
                                    res.onSuccess { profile ->
                                        Toast.makeText(context, "Welcome, ${profile.name}!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(profile)
                                    }.onFailure { err ->
                                        Toast.makeText(context, err.message ?: "Registration error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("register_skip_button")
                        ) {
                            Text("Skip for now", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Already have an account? Log In",
                        color = FacebookBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isRegistering = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Frndom © 2026",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
