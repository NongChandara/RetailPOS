package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advertisements")
data class AdvertisementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val khmerTitle: String = "",
    val description: String = "",
    val category: String = "Service", // "Cosmetics", "Coffee", "Service"
    val badgeText: String = "VIP Booking 👑",
    val price: Double = 35.00,
    val imageUrl: String = "",
    val buttonText: String = "Book an Appointment →",
    val isRunning: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
