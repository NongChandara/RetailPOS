package com.example.ui.auth

enum class AuthBoardTab(val title: String) {
    SIGN_IN("Sign In"),
    SIGN_UP("Sign Up"),
    QUICK_PROFILES("Quick Accounts");
}

data class UnifiedQuickAccount(
    val name: String,
    val email: String,
    val username: String,
    val pin: String,
    val role: String,
    val business: String,
    val iconEmoji: String
)
