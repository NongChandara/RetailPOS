package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val tier: String = "BRONZE", // "BRONZE", "SILVER", "GOLD", "VIP"
    val loyaltyPoints: Int = 0,
    val totalSpent: Double = 0.0,
    val visitsCount: Int = 0,
    val favoriteOrder: String = "",
    val notes: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val tierColorHex: String
        get() = when (tier.uppercase()) {
            "VIP" -> "#7C3AED"      // Purple
            "GOLD" -> "#D97706"     // Amber / Gold
            "SILVER" -> "#64748B"   // Slate / Silver
            else -> "#B45309"       // Bronze
        }

    val tierDisplayName: String
        get() = when (tier.uppercase()) {
            "VIP" -> "VIP Member ⭐"
            "GOLD" -> "Gold Member 👑"
            "SILVER" -> "Silver Member 🥈"
            else -> "Bronze Member 🥉"
        }
}
