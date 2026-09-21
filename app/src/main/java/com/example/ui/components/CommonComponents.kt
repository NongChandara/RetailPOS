package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusAmberContainer
import com.example.ui.theme.RetailStatusBlue
import com.example.ui.theme.RetailStatusBlueContainer
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusGreenContainer
import com.example.ui.theme.RetailStatusPurple
import com.example.ui.theme.RetailStatusPurpleContainer
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailStatusRedContainer
import com.example.util.CurrencyMode
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(
    amount: Double,
    mode: CurrencyMode = CurrencyMode.DUAL,
    rate: Double = CurrencyUtils.DEFAULT_KHR_EXCHANGE_RATE
): String {
    return CurrencyUtils.formatCurrency(amount, mode, rate)
}

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}

fun formatShortTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
    return sdf.format(Date(timestamp))
}

@Composable
fun StockBadge(
    quantity: Int,
    threshold: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        quantity <= 0 -> Triple(RetailStatusRedContainer, RetailStatusRed, "Out of Stock")
        quantity <= threshold -> Triple(RetailStatusAmberContainer, RetailStatusAmber, "Low ($quantity left)")
        else -> Triple(RetailStatusGreenContainer, RetailStatusGreen, "$quantity in stock")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (role.uppercase()) {
        "ADMIN" -> Pair(RetailStatusPurpleContainer, RetailStatusPurple)
        "CASHIER" -> Pair(RetailStatusBlueContainer, RetailStatusBlue)
        else -> Pair(RetailSlate100, RetailSlate700)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = role.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}
