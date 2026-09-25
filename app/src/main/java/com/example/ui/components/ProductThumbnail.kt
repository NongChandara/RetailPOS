package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailTealPrimary

/**
 * Curated product image catalog providing high-resolution product photography
 * optimized for POS product catalogs and receipts.
 */
object ProductImageCatalog {
    // Preset image choices for Add/Edit Product dialog
    data class PresetImage(
        val label: String,
        val category: String,
        val url: String,
        val icon: ImageVector
    )

    val PRESET_IMAGES = listOf(
        PresetImage(
            label = "Cold Brew Coffee",
            category = "Beverages",
            url = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.LocalCafe
        ),
        PresetImage(
            label = "Iced Latte / Coffee",
            category = "Beverages",
            url = "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.LocalCafe
        ),
        PresetImage(
            label = "Spring Water",
            category = "Beverages",
            url = "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.LocalCafe
        ),
        PresetImage(
            label = "Sourdough Bread",
            category = "Bakery",
            url = "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.BakeryDining
        ),
        PresetImage(
            label = "Butter Croissant",
            category = "Bakery",
            url = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.BakeryDining
        ),
        PresetImage(
            label = "Dark Chocolate",
            category = "Snacks",
            url = "https://images.unsplash.com/photo-1548907040-4baa42d10919?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Fastfood
        ),
        PresetImage(
            label = "Potato Chips",
            category = "Snacks",
            url = "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Fastfood
        ),
        PresetImage(
            label = "USB-C Fast Cable",
            category = "Electronics",
            url = "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Devices
        ),
        PresetImage(
            label = "Wireless Mouse",
            category = "Electronics",
            url = "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Devices
        ),
        PresetImage(
            label = "Bamboo Cutlery Set",
            category = "Home & Goods",
            url = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Home
        ),
        PresetImage(
            label = "Lip Balm",
            category = "Personal Care",
            url = "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Spa
        ),
        PresetImage(
            label = "Vitamin C Glow Serum",
            category = "Personal Care",
            url = "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Spa
        ),
        PresetImage(
            label = "SPF 50+ Sunscreen",
            category = "Personal Care",
            url = "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Spa
        ),
        PresetImage(
            label = "Canvas Tote Bag",
            category = "Apparel",
            url = "https://images.unsplash.com/photo-1597484661643-2f5fef640dd1?auto=format&fit=crop&w=400&q=80",
            icon = Icons.Filled.Checkroom
        )
    )

    /**
     * Resolves an image URL for any item. If customUrl is provided, it uses it.
     * Otherwise it matches by product name keywords or category so every item in the APK
     * has a clean, attractive image.
     */
    fun resolveImageUrl(customUrl: String?, productName: String, category: String): String {
        if (!customUrl.isNullOrBlank()) return customUrl

        val lowerName = productName.lowercase()
        return when {
            lowerName.contains("cold brew") -> "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("water") -> "https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("coffee") || lowerName.contains("latte") || lowerName.contains("cappuccino") -> "https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("bread") || lowerName.contains("sourdough") -> "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("croissant") || lowerName.contains("bakery") -> "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("chocolate") -> "https://images.unsplash.com/photo-1548907040-4baa42d10919?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("chip") || lowerName.contains("snack") -> "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("cable") || lowerName.contains("charger") -> "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("mouse") -> "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("cutlery") || lowerName.contains("bamboo") -> "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("balm") || lowerName.contains("lipstick") -> "https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("serum") -> "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("sunscreen") || lowerName.contains("spf") -> "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=400&q=80"
            lowerName.contains("tote") || lowerName.contains("bag") || lowerName.contains("apparel") -> "https://images.unsplash.com/photo-1597484661643-2f5fef640dd1?auto=format&fit=crop&w=400&q=80"
            category.equals("Beverages", ignoreCase = true) -> "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=80"
            category.equals("Bakery", ignoreCase = true) -> "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?auto=format&fit=crop&w=400&q=80"
            category.equals("Snacks", ignoreCase = true) -> "https://images.unsplash.com/photo-1566478989037-eec170784d0b?auto=format&fit=crop&w=400&q=80"
            category.equals("Electronics", ignoreCase = true) -> "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=400&q=80"
            category.equals("Home & Goods", ignoreCase = true) -> "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=400&q=80"
            category.equals("Personal Care", ignoreCase = true) -> "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=400&q=80"
            category.equals("Apparel", ignoreCase = true) -> "https://images.unsplash.com/photo-1597484661643-2f5fef640dd1?auto=format&fit=crop&w=400&q=80"
            else -> "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=400&q=80"
        }
    }

    fun getCategoryColors(category: String): Pair<Color, Color> {
        return when (category.lowercase()) {
            "beverages" -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7)) // Light Sky -> Cyan
            "bakery" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Light Amber -> Amber
            "snacks" -> Pair(Color(0xFFD1FAE5), Color(0xFF059669)) // Light Emerald -> Emerald
            "electronics" -> Pair(Color(0xFFE0E7FF), Color(0xFF4F46E5)) // Light Indigo -> Indigo
            "home & goods", "home" -> Pair(Color(0xFFEDE9FE), Color(0xFF7C3AED)) // Light Violet -> Purple
            "personal care" -> Pair(Color(0xFFFCE7F3), Color(0xFFDB2777)) // Light Pink -> Pink
            "apparel" -> Pair(Color(0xFFCCFBF1), Color(0xFF0D9488)) // Light Teal -> Teal
            else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569)) // Slate
        }
    }

    fun getCategoryIcon(category: String): ImageVector {
        return when (category.lowercase()) {
            "beverages" -> Icons.Filled.LocalCafe
            "bakery" -> Icons.Filled.BakeryDining
            "snacks" -> Icons.Filled.Fastfood
            "electronics" -> Icons.Filled.Devices
            "home & goods", "home" -> Icons.Filled.Home
            "personal care" -> Icons.Filled.Spa
            "apparel" -> Icons.Filled.Checkroom
            else -> Icons.Filled.ShoppingBag
        }
    }
}

/**
 * Reusable, high-performance product thumbnail with smooth image caching,
 * graceful placeholder, category-styled fallback, and crisp border styling.
 */
@Composable
fun ProductThumbnail(
    imageUrl: String?,
    productName: String,
    category: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    contentDescription: String? = productName
) {
    val finalUrl = ProductImageCatalog.resolveImageUrl(imageUrl, productName, category)
    val (bgColor, accentColor) = ProductImageCatalog.getCategoryColors(category)
    val categoryIcon = ProductImageCatalog.getCategoryIcon(category)
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(0.8.dp, RetailSlate300.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(finalUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = RetailTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            error = {
                // Fallback display if network image fails or is unavailable offline
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(bgColor, bgColor.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        )
    }
}
