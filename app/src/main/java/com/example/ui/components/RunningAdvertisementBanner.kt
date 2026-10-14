package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.AdvertisementEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailTealPrimary
import com.example.util.CurrencyUtils
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Curated Preset Images for Web and In-App Advertising
 */
data class ImagePreset(
    val name: String,
    val category: String,
    val url: String
)

val PRESET_AD_IMAGES = listOf(
    // ☕ Coffee & Beverages
    ImagePreset("Espresso & Latte", "Coffee", "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Matcha Latte Cream", "Coffee", "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Caramel Macchiato", "Coffee", "https://images.unsplash.com/photo-1485808191629-56ade4408d21?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Cold Brew Citrus", "Coffee", "https://images.unsplash.com/photo-1517256064527-09c73fc73e38?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Iced Passion Tea", "Coffee", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=800&q=80"),

    // 👑 Spa, Salon & Wellness
    ImagePreset("Hydra Facial Spa", "Service", "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Aroma Body Massage", "Service", "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Foot Herbal Care", "Service", "https://images.unsplash.com/photo-1519823551278-64ac92734fb1?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Hair Styling & Wash", "Service", "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=800&q=80"),

    // 💄 Cosmetics & Skincare
    ImagePreset("Botanical Serum", "Cosmetics", "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Sunscreen Cream", "Cosmetics", "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Organic Lip Care", "Cosmetics", "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Hydrating Essence", "Cosmetics", "https://images.unsplash.com/photo-1608248597359-009772a6b281?auto=format&fit=crop&w=800&q=80"),

    // 🥐 Bakery & Food
    ImagePreset("Artisan Croissant", "Bakery", "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=800&q=80"),
    ImagePreset("Strawberry Tart", "Bakery", "https://images.unsplash.com/photo-1464305795204-6f5bbfc7fb81?auto=format&fit=crop&w=800&q=80"),

    // 🔥 Special Promotions
    ImagePreset("Flash Sale Deal", "Promo", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=800&q=80")
)

/**
 * Running Advertisement Banner Composable
 * Displays rotating promotional cards with editable images, custom badges,
 * and support for adding new ads from Web URLs or device picker.
 */
@Composable
fun RunningAdvertisementBanner(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val allAds by viewModel.allAdvertisements.collectAsState()
    val runningAds by viewModel.runningAdvertisements.collectAsState()
    val showBanner by viewModel.showAdBanner.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isAutoRunning by remember { mutableStateOf(true) }
    var cycleSpeedSeconds by remember { mutableIntStateOf(5) }
    var isCollapsed by remember { mutableStateOf(false) }

    var adToEdit by remember { mutableStateOf<AdvertisementEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showManageAllDialog by remember { mutableStateOf(false) }
    var showFullscreenDisplay by remember { mutableStateOf(false) }

    // Filter ads according to category selection
    val filteredAds = remember(runningAds, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            runningAds
        } else {
            runningAds.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    // Keep currentIndex valid
    val safeIndex = if (filteredAds.isNotEmpty()) {
        currentIndex.coerceIn(0, filteredAds.size - 1)
    } else 0

    // Auto-scroll running timer
    LaunchedEffect(isAutoRunning, filteredAds.size, safeIndex, cycleSpeedSeconds) {
        if (isAutoRunning && filteredAds.size > 1) {
            delay(cycleSpeedSeconds * 1000L)
            currentIndex = (safeIndex + 1) % filteredAds.size
        }
    }

    if (!showBanner) return

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("running_advertisement_banner")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Category Tabs + Running Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    val categories = listOf(
                        Triple("All", "All", null),
                        Triple("Cosmetics", "💄 Cosmetics", Color(0xFFBE185D)),
                        Triple("Coffee", "☕ Coffee", Color(0xFF92400E)),
                        Triple("Service", "👑 Service", Color(0xFF6D28D9)),
                        Triple("Bakery", "🥐 Bakery", Color(0xFFD97706))
                    )

                    items(categories) { (catKey, label, activeColor) ->
                        val isSelected = selectedCategoryFilter.equals(catKey, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                isSelected && activeColor != null -> activeColor
                                isSelected -> RetailSlate900
                                else -> RetailSlate100
                            },
                            border = if (!isSelected) BorderStroke(1.dp, RetailSlate300) else null,
                            modifier = Modifier
                                .clickable {
                                    selectedCategoryFilter = catKey
                                    currentIndex = 0
                                }
                                .testTag("ad_filter_$catKey")
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else RetailSlate700,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Controls: Live status, Speed, Display Mode, Settings, Collapse
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Running Live Pulse Toggle
                    Surface(
                        color = if (isAutoRunning) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable { isAutoRunning = !isAutoRunning }
                            .testTag("ad_auto_run_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAutoRunning) Color(0xFF16A34A) else RetailSlate500)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAutoRunning) "Live Ad (${cycleSpeedSeconds}s)" else "Paused",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoRunning) Color(0xFF15803D) else RetailSlate700
                            )
                        }
                    }

                    // Fullscreen Web / Customer Display Mode
                    IconButton(
                        onClick = { showFullscreenDisplay = true },
                        modifier = Modifier.size(28.dp).testTag("fullscreen_display_btn")
                    ) {
                        Icon(
                            Icons.Filled.Tv,
                            contentDescription = "Customer Display Mode",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Manage / Add All Ads Button
                    IconButton(
                        onClick = { showManageAllDialog = true },
                        modifier = Modifier.size(28.dp).testTag("manage_ads_btn")
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Manage Ads",
                            tint = RetailSlate700,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Collapse / Expand toggle
                    IconButton(
                        onClick = { isCollapsed = !isCollapsed },
                        modifier = Modifier.size(28.dp).testTag("collapse_ad_btn")
                    ) {
                        Icon(
                            if (isCollapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Toggle Ad View",
                            tint = RetailSlate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = !isCollapsed) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (filteredAds.isEmpty()) {
                        // Empty State: Prompt to add advertisement
                        Surface(
                            color = Color(0xFFFAF5FF),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.Campaign, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No Active Running Ads in '$selectedCategoryFilter'",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF581C87)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add promotional images or choose web presets to run on this advertising banner.",
                                    fontSize = 11.sp,
                                    color = RetailSlate500
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Add Running Advertisement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        val currentAd = filteredAds[safeIndex]

                        // Animated Ad Card Presentation
                        AnimatedContent(
                            targetState = currentAd,
                            transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(250)) },
                            label = "ad_card_transition"
                        ) { ad ->
                            AdvertisementCard(
                                ad = ad,
                                onEdit = { adToEdit = ad },
                                onBookOrOrder = { viewModel.bookOrOrderAdvertisement(ad) }
                            )
                        }

                        // Navigation dots and controls below the card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous / Next buttons & Dots
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        currentIndex = if (safeIndex > 0) safeIndex - 1 else filteredAds.size - 1
                                    },
                                    modifier = Modifier.size(26.dp).testTag("prev_ad_btn")
                                ) {
                                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev", tint = RetailSlate700)
                                }

                                // Dot indicators
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    filteredAds.forEachIndexed { idx, _ ->
                                        val isActive = idx == safeIndex
                                        Box(
                                            modifier = Modifier
                                                .size(if (isActive) 8.dp else 5.dp)
                                                .clip(CircleShape)
                                                .background(if (isActive) Color(0xFF6D28D9) else RetailSlate300)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        currentIndex = (safeIndex + 1) % filteredAds.size
                                    },
                                    modifier = Modifier.size(26.dp).testTag("next_ad_btn")
                                ) {
                                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = RetailSlate700)
                                }
                            }

                            // Quick Action Buttons: Edit Image/Ad & Add Ad
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { adToEdit = currentAd },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp).testTag("quick_edit_active_ad_btn")
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, tint = RetailSlate700, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Edit Image & Ad", fontSize = 10.sp, color = RetailSlate700)
                                }

                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp).testTag("quick_add_new_ad_btn")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ New Ad", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Advertisement Dialog
    if (showAddDialog) {
        EditAdvertisementDialog(
            ad = null,
            onDismiss = { showAddDialog = false },
            onSave = { updated ->
                viewModel.addAdvertisement(
                    title = updated.title,
                    khmerTitle = updated.khmerTitle,
                    description = updated.description,
                    category = updated.category,
                    badgeText = updated.badgeText,
                    price = updated.price,
                    imageUrl = updated.imageUrl,
                    buttonText = updated.buttonText,
                    isRunning = updated.isRunning
                )
                showAddDialog = false
            }
        )
    }

    // Edit Advertisement Dialog
    adToEdit?.let { targetAd ->
        EditAdvertisementDialog(
            ad = targetAd,
            onDismiss = { adToEdit = null },
            onDelete = {
                viewModel.deleteAdvertisement(targetAd.id)
                adToEdit = null
            },
            onSave = { updated ->
                viewModel.updateAdvertisement(updated)
                adToEdit = null
            }
        )
    }

    // Manage All Advertisements Dialog
    if (showManageAllDialog) {
        ManageAllAdvertisementsDialog(
            allAds = allAds,
            cycleSpeed = cycleSpeedSeconds,
            onSpeedChange = { cycleSpeedSeconds = it },
            onDismiss = { showManageAllDialog = false },
            onAddNew = {
                showManageAllDialog = false
                showAddDialog = true
            },
            onEditAd = { ad ->
                showManageAllDialog = false
                adToEdit = ad
            },
            onToggleRunning = { id, isRunning ->
                viewModel.toggleAdRunning(id, isRunning)
            },
            onDeleteAd = { id ->
                viewModel.deleteAdvertisement(id)
            }
        )
    }

    // Fullscreen Customer Display Mode (Digital Signage on Web / Screen)
    if (showFullscreenDisplay) {
        CustomerDisplayModeDialog(
            ads = runningAds,
            cycleSpeedSeconds = cycleSpeedSeconds,
            onDismiss = { showFullscreenDisplay = false },
            onOrderAd = { ad ->
                viewModel.bookOrOrderAdvertisement(ad)
                showFullscreenDisplay = false
            }
        )
    }
}

/**
 * Advertisement Card displaying photography with overlays and quick actions
 */
@Composable
fun AdvertisementCard(
    ad: AdvertisementEntity,
    onEdit: () -> Unit,
    onBookOrOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        color = Color(0xFFFAF7F5),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Main Hero Image with Badge Overlay & Tap-to-Edit affordance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0))
                    .clickable { onEdit() }
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(ad.imageUrl.ifBlank { "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=800&q=80" })
                        .crossfade(true)
                        .build(),
                    contentDescription = ad.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    loading = {
                        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp), color = Color(0xFF6D28D9))
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE), Color(0xFFC4B5FD))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("TR Coffee & Retail", fontWeight = FontWeight.Bold, color = Color(0xFF581C87), fontSize = 12.sp)
                            }
                        }
                    }
                )

                // Top-Left Category Badge
                Surface(
                    color = when (ad.category.lowercase()) {
                        "coffee" -> Color(0xFF92400E)
                        "cosmetics" -> Color(0xFFBE185D)
                        "bakery" -> Color(0xFFD97706)
                        else -> Color(0xFF6D28D9)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = ad.category,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Top-Right Badge: e.g. "VIP Booking 👑"
                if (ad.badgeText.isNotBlank()) {
                    Surface(
                        color = Color(0xFF6D28D9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .testTag("ad_card_badge")
                    ) {
                        Text(
                            text = ad.badgeText,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom-Left Overlay Chip: "📷 Tap to Edit Image"
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clickable { onEdit() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Edit Image & Ad", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title & Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ad.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = RetailSlate900,
                    modifier = Modifier.weight(1f)
                )

                if (ad.price > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyUtils.formatUsd(ad.price),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6D28D9)
                        )
                        Text(
                            text = CurrencyUtils.formatKhr(ad.price * 4100.0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            // Subtitle in Khmer
            if (ad.khmerTitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = ad.khmerTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF7C3AED)
                )
            }

            // Detailed Description
            if (ad.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ad.description,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = RetailSlate700
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button: e.g. "Book an Appointment →" or "Add to Cart"
            Button(
                onClick = onBookOrOrder,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_action_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = ad.buttonText.ifBlank { "Book an Appointment →" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Edit / Create Advertisement Dialog with Interactive Image Editor & Picker
 */
@Composable
fun EditAdvertisementDialog(
    ad: AdvertisementEntity?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (AdvertisementEntity) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(ad?.title ?: "Hydra-Glow Signature Facial") }
    var khmerTitle by remember { mutableStateOf(ad?.khmerTitle ?: "សេវាកម្មព្យាបាល និងថែទាំស្បែកមុខ 60 នាទី") }
    var description by remember { mutableStateOf(ad?.description ?: "Deep skin cleansing, ultrasound infusion & botanical calming mask.") }
    var category by remember { mutableStateOf(ad?.category ?: "Service") }
    var badgeText by remember { mutableStateOf(ad?.badgeText ?: "VIP Booking 👑") }
    var priceText by remember { mutableStateOf(if (ad != null) String.format(Locale.US, "%.2f", ad.price) else "35.00") }
    var imageUrl by remember { mutableStateOf(ad?.imageUrl ?: "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=800&q=80") }
    var buttonText by remember { mutableStateOf(ad?.buttonText ?: "Book an Appointment →") }
    var isRunning by remember { mutableStateOf(ad?.isRunning ?: true) }

    var selectedPresetCategory by remember { mutableStateOf("All") }

    // Android Zero-Permission Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    val categories = listOf("Service", "Cosmetics", "Coffee", "Bakery", "Promo")

    val quickBadges = listOf(
        "VIP Booking 👑",
        "Special Promo 🔥",
        "50% OFF ⚡",
        "BUY 1 GET 1 🎁",
        "Today's Special ☕",
        "New Arrival ✨"
    )

    val quickCtas = listOf(
        "Book an Appointment →",
        "Order Now 🛒",
        "Claim Promo Deal 🎁",
        "Add to Cart →"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (ad == null) "Add Running Advertisement" else "Edit Advertisement & Image",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = RetailSlate900
                        )
                        Text(
                            text = "Set promotional photography, badges, and pricing for running ads",
                            fontSize = 11.sp,
                            color = RetailSlate500
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate700)
                    }
                }

                HorizontalDivider()

                // 1. LIVE IMAGE PREVIEW & EDIT SECTION
                Text(
                    text = "1. Advertisement Image & Visual Preview",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )

                // Live Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .testTag("dialog_image_preview")
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl.ifBlank { "https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?auto=format&fit=crop&w=800&q=80" })
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        loading = {
                            Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp), color = Color(0xFF6D28D9))
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFEDE9FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Image, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(36.dp))
                                    Text("Image Preview", color = Color(0xFF581C87), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    )

                    // Live Badge Overlay
                    if (badgeText.isNotBlank()) {
                        Surface(
                            color = Color(0xFF6D28D9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Live Running Indicator
                    Surface(
                        color = if (isRunning) Color(0xFF16A34A) else RetailSlate700,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isRunning) "● RUNNING ON DISPLAY" else "○ PAUSED",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // Image Source Options: Pick Photo OR Enter Web URL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo Picker Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f).testTag("pick_device_photo_btn")
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Device Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Clear or Reset Image URL button
                    OutlinedButton(
                        onClick = {
                            imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=800&q=80"
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("Default", fontSize = 11.sp)
                    }
                }

                // Web Image URL Input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Web Image URL (HTTPS)") },
                    placeholder = { Text("https://images.unsplash.com/...") },
                    leadingIcon = { Icon(Icons.Filled.Link, contentDescription = null, tint = RetailSlate500) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ad_image_url_field")
                )

                // Curated Web Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Or Choose Curated Web Preset:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                        Text("${PRESET_AD_IMAGES.size} web photos", fontSize = 10.sp, color = RetailSlate500)
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PRESET_AD_IMAGES) { preset ->
                            val isSelected = imageUrl == preset.url
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFFEDE9FE) else RetailSlate100,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF7C3AED) else RetailSlate300),
                                modifier = Modifier
                                    .clickable { imageUrl = preset.url }
                                    .testTag("preset_${preset.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF6D28D9) else RetailSlate700
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 2. TEXT & PROMOTION CONTENT
                Text(
                    text = "2. Title & Offer Details",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Advertisement Title (English)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ad_title_field")
                )

                OutlinedTextField(
                    value = khmerTitle,
                    onValueChange = { khmerTitle = it },
                    label = { Text("Khmer Title / Subtitle (ចំណងជើងខ្មែរ)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ad_khmer_title_field")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Highlights") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("ad_price_field")
                    )

                    OutlinedTextField(
                        value = badgeText,
                        onValueChange = { badgeText = it },
                        label = { Text("Badge Label") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).testTag("ad_badge_field")
                    )
                }

                // Quick Badges
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickBadges) { qb ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (badgeText == qb) Color(0xFF6D28D9) else RetailSlate100,
                            modifier = Modifier.clickable { badgeText = qb }
                        ) {
                            Text(
                                text = qb,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (badgeText == qb) Color.White else RetailSlate700,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Category Selection
                Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate700)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        val isSelected = category.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Call to Action (CTA) Button Text
                OutlinedTextField(
                    value = buttonText,
                    onValueChange = { buttonText = it },
                    label = { Text("Action Button Text (CTA)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("ad_button_text_field")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickCtas) { cta ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (buttonText == cta) Color(0xFF6D28D9) else RetailSlate100,
                            modifier = Modifier.clickable { buttonText = cta }
                        ) {
                            Text(
                                text = cta,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (buttonText == cta) Color.White else RetailSlate700,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                // 3. RUNNING STATUS TOGGLE
                Surface(
                    color = if (isRunning) Color(0xFFF0FDF4) else RetailSlate100,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isRunning) Color(0xFF86EFAC) else RetailSlate300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isRunning) "Running Status: ACTIVE (Cycling on Banner)" else "Running Status: PAUSED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isRunning) Color(0xFF166534) else RetailSlate700
                            )
                            Text(
                                text = "Show this ad in the rotating customer promotion banner",
                                fontSize = 10.sp,
                                color = RetailSlate500
                            )
                        }

                        Switch(
                            checked = isRunning,
                            onCheckedChange = { isRunning = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF16A34A)
                            ),
                            modifier = Modifier.testTag("ad_running_switch")
                        )
                    }
                }

                // Action Buttons: Save & Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (ad != null && onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Ad", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
                                val updated = AdvertisementEntity(
                                    id = ad?.id ?: 0L,
                                    title = title,
                                    khmerTitle = khmerTitle,
                                    description = description,
                                    category = category,
                                    badgeText = badgeText,
                                    price = parsedPrice,
                                    imageUrl = imageUrl,
                                    buttonText = buttonText,
                                    isRunning = isRunning
                                )
                                onSave(updated)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_ad_button")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (ad == null) "Create & Run Ad" else "Save & Run Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Manage All Advertisements Dialog (List, toggle running, edit, delete, rotation speed)
 */
@Composable
fun ManageAllAdvertisementsDialog(
    allAds: List<AdvertisementEntity>,
    cycleSpeed: Int,
    onSpeedChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAddNew: () -> Unit,
    onEditAd: (AdvertisementEntity) -> Unit,
    onToggleRunning: (Long, Boolean) -> Unit,
    onDeleteAd: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .height(520.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manage Running Advertisements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RetailSlate900
                        )
                        Text(
                            text = "${allAds.size} total ads • ${allAds.count { it.isRunning }} currently running",
                            fontSize = 11.sp,
                            color = RetailSlate500
                        )
                    }

                    Button(
                        onClick = onAddNew,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ New Ad", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Rotation Speed Selector
                Surface(
                    color = Color(0xFFFAF5FF),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Schedule, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Running Rotation Speed:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RetailSlate900)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(3, 5, 8, 10).forEach { sec ->
                                val isSelected = cycleSpeed == sec
                                Surface(
                                    color = if (isSelected) Color(0xFF7C3AED) else Color.White,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF7C3AED) else RetailSlate300),
                                    modifier = Modifier.clickable { onSpeedChange(sec) }
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else RetailSlate700,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allAds, key = { it.id }) { ad ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (ad.isRunning) Color(0xFFFAF5FF) else RetailSlate100),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (ad.isRunning) Color(0xFFE9D5FF) else RetailSlate300)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ad.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = RetailSlate900
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFF6D28D9),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = ad.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (ad.khmerTitle.isNotBlank()) {
                                        Text(
                                            text = ad.khmerTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF7C3AED)
                                        )
                                    }

                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", ad.price)} • ${ad.buttonText}",
                                        fontSize = 11.sp,
                                        color = RetailSlate700
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Running toggle switch
                                    Switch(
                                        checked = ad.isRunning,
                                        onCheckedChange = { onToggleRunning(ad.id, it) },
                                        modifier = Modifier.padding(end = 4.dp),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF7C3AED)
                                        )
                                    )

                                    IconButton(
                                        onClick = { onEditAd(ad) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RetailSlate700, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { onDeleteAd(ad.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Customer Display Mode (Web Digital Signage)
 * Displays running advertisements in big showcase layout for web presentation or customer monitor.
 */
@Composable
fun CustomerDisplayModeDialog(
    ads: List<AdvertisementEntity>,
    cycleSpeedSeconds: Int,
    onDismiss: () -> Unit,
    onOrderAd: (AdvertisementEntity) -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var progress by remember { mutableStateOf(0f) }

    val safeIndex = if (ads.isNotEmpty()) currentIndex.coerceIn(0, ads.size - 1) else 0

    // Timer cycling with progress animation
    LaunchedEffect(ads.size, safeIndex, cycleSpeedSeconds) {
        if (ads.size > 1) {
            val totalSteps = cycleSpeedSeconds * 10
            for (step in 1..totalSteps) {
                delay(100)
                progress = step.toFloat() / totalSteps.toFloat()
            }
            progress = 0f
            currentIndex = (safeIndex + 1) % ads.size
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxSize()
        ) {
            if (ads.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Advertisements Currently Running", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) { Text("Exit Display") }
                    }
                }
            } else {
                val currentAd = ads[safeIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Bar: Exit button & Ad indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF10B981),
                                shape = CircleShape,
                                modifier = Modifier.size(10.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TR COFFEE CUSTOMER DISPLAY (LIVE)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Filled.FullscreenExit, contentDescription = "Exit Display", tint = Color.White)
                        }
                    }

                    // Progress bar for cycling timer
                    if (ads.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color(0xFF8B5CF6),
                            trackColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Showcase Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Large Hero Image
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(currentAd.imageUrl.ifBlank { "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=800&q=80" })
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = currentAd.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Overlay Badge
                                if (currentAd.badgeText.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFF7C3AED),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = currentAd.badgeText,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Details & Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentAd.title,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    if (currentAd.khmerTitle.isNotBlank()) {
                                        Text(
                                            text = currentAd.khmerTitle,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFA78BFA)
                                        )
                                    }
                                    if (currentAd.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentAd.description,
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                if (currentAd.price > 0.0) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyUtils.formatUsd(currentAd.price),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFACC15)
                                        )
                                        Text(
                                            text = CurrencyUtils.formatKhr(currentAd.price * 4100.0),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action button: Order / Book Now
                            Button(
                                onClick = { onOrderAd(currentAd) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = currentAd.buttonText.ifBlank { "Book / Order Now →" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex = if (safeIndex > 0) safeIndex - 1 else ads.size - 1
                            }
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev", tint = Color.White)
                        }

                        // Dot indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ads.forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(if (idx == safeIndex) 10.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (idx == safeIndex) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                currentIndex = (safeIndex + 1) % ads.size
                            }
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
