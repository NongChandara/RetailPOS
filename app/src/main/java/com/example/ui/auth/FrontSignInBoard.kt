package com.example.ui.auth

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun FrontSignInBoard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AuthBoardTab.SIGN_IN) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isFeedbackError by remember { mutableStateOf(false) }

    // Clear auto fill on front sign in (Empty defaults)
    var signInIdentifier by remember { mutableStateOf("") }
    var signInSecret by remember { mutableStateOf("") }
    var secretVisible by remember { mutableStateOf(false) }

    // Sign Up fields
    var signUpBizName by remember { mutableStateOf("") }
    var signUpFullName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpRole by remember { mutableStateOf("CASHIER") }
    var signUpPin by remember { mutableStateOf("") }
    var signUpPinVisible by remember { mutableStateOf(false) }
    var signUpTelegram by remember { mutableStateOf("") }

    val quickProfiles = listOf(
        UnifiedQuickAccount("Nong Chandara", "nong.chandara@gmail.com", "chandara", "1234", "ADMIN", "TR Store & Cafe", "👑"),
        UnifiedQuickAccount("Sophea Cashier", "sophea@trcoffee.kh", "sophea", "1111", "CASHIER", "TR Store & Cafe", "☕"),
        UnifiedQuickAccount("Dara Staff", "dara@trcoffee.kh", "dara", "2222", "STAFF", "TR Store & Cafe", "🧴")
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF0D9488), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "TR Store & Cafe",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Sign in to access POS, Inventory & Reports",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF14B8A6),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = Color(0xFF14B8A6)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AuthBoardTab.values().forEach { tab ->
                        Tab(
                            selected = activeTab == tab,
                            onClick = {
                                activeTab = tab
                                feedbackMessage = null
                            },
                            text = {
                                Text(
                                    tab.title,
                                    color = if (activeTab == tab) Color(0xFF14B8A6) else Color(0xFF94A3B8),
                                    fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                feedbackMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFeedbackError) Color(0xFF7F1D1D) else Color(0xFF065F46),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                when (activeTab) {
                    AuthBoardTab.SIGN_IN -> {
                        OutlinedTextField(
                            value = signInIdentifier,
                            onValueChange = { signInIdentifier = it },
                            label = { Text("Email, Username or PIN", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_identifier_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = signInSecret,
                            onValueChange = { signInSecret = it },
                            label = { Text("Password or 4-digit PIN", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            trailingIcon = {
                                IconButton(onClick = { secretVisible = !secretVisible }) {
                                    Icon(
                                        if (secretVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Secret",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_secret_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (signInIdentifier.isBlank()) {
                                    feedbackMessage = "Please enter your username, email, or PIN"
                                    isFeedbackError = true
                                    return@Button
                                }
                                viewModel.signInUser(signInIdentifier.trim(), signInSecret.trim()) { user ->
                                    if (user != null) {
                                        feedbackMessage = "Welcome back, ${user.name}!"
                                        isFeedbackError = false
                                    } else {
                                        feedbackMessage = "Invalid credentials. Please verify your PIN or password."
                                        isFeedbackError = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signin_button")
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Accounts Hint
                        Text(
                            text = "Quick Demo Accounts:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickProfiles.forEach { acc ->
                                Surface(
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            signInIdentifier = acc.email
                                            signInSecret = acc.pin
                                        }
                                        .testTag("quick_fill_${acc.role.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(acc.iconEmoji, fontSize = 18.sp)
                                        Text(
                                            acc.name.split(" ").first(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            acc.role,
                                            fontSize = 9.sp,
                                            color = Color(0xFF14B8A6)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AuthBoardTab.SIGN_UP -> {
                        OutlinedTextField(
                            value = signUpBizName,
                            onValueChange = { signUpBizName = it },
                            label = { Text("Store / Branch Name", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpFullName,
                            onValueChange = { signUpFullName = it },
                            label = { Text("Staff Full Name", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpEmail,
                            onValueChange = { signUpEmail = it },
                            label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Role locked to CASHIER for all new accounts
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth().testTag("new_account_role_notice")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF2DD4BF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Account Role: Cashier",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Access limited to POS & Product Menu. Admin can promote role.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                                Surface(
                                    color = Color(0xFF0F766E),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "CASHIER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpPin,
                            onValueChange = { signUpPin = it },
                            label = { Text("PIN (e.g. 1234)", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            trailingIcon = {
                                IconButton(onClick = { signUpPinVisible = !signUpPinVisible }) {
                                    Icon(
                                        if (signUpPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle PIN",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            visualTransformation = if (signUpPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signUpTelegram,
                            onValueChange = { signUpTelegram = it },
                            label = { Text("Telegram Username (Optional)", color = Color(0xFF94A3B8)) },
                            leadingIcon = {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF14B8A6))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF14B8A6),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (signUpFullName.isBlank() || signUpPin.isBlank()) {
                                    feedbackMessage = "Staff name and PIN are required"
                                    isFeedbackError = true
                                    return@Button
                                }
                                viewModel.signUpNewUser(
                                    businessName = signUpBizName.ifBlank { "TR Store & Cafe" },
                                    name = signUpFullName.trim(),
                                    email = signUpEmail.trim(),
                                    role = "CASHIER",
                                    pin = signUpPin.trim(),
                                    telegram = signUpTelegram.trim()
                                )
                                feedbackMessage = "Account created! You can now sign in."
                                isFeedbackError = false
                                activeTab = AuthBoardTab.SIGN_IN
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("signup_button")
                        ) {
                            Text("Create Staff Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    AuthBoardTab.QUICK_PROFILES -> {
                        Text(
                            text = "Select a profile to switch immediately:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            quickProfiles.forEach { acc ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.signInUser(acc.email, acc.pin) { user ->
                                                if (user != null) {
                                                    feedbackMessage = "Switched to ${user.name}"
                                                    isFeedbackError = false
                                                }
                                            }
                                        }
                                        .testTag("select_profile_${acc.role.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(acc.iconEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(acc.name, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("${acc.role} • PIN: ${acc.pin}", fontSize = 12.sp, color = Color(0xFF14B8A6))
                                            Text(acc.email, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF14B8A6),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
