package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BranchEntity
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate800
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailTealDark
import com.example.ui.theme.RetailTealPrimary
import java.util.Locale

/**
 * Comprehensive M3 Dialog to edit receipt configuration, layout spacing gap,
 * and tax percent for any branch in the retail POS system.
 */
@Composable
fun BranchReceiptAndTaxDialog(
    branches: List<BranchEntity>,
    activeBranch: BranchEntity?,
    initialBranch: BranchEntity? = null,
    onDismiss: () -> Unit,
    onSaveBranchReceipt: (
        branch: BranchEntity,
        header: String,
        subtitle: String,
        vatTin: String,
        address: String,
        phone: String,
        footer: String,
        taxPercent: Double,
        receiptGap: Int,
        setAsActive: Boolean
    ) -> Unit,
    onSetActiveBranch: (BranchEntity) -> Unit
) {
    var selectedBranch by remember {
        mutableStateOf(initialBranch ?: activeBranch ?: branches.firstOrNull() ?: BranchEntity(name = "Main Branch"))
    }

    // Editable form state initialized from selectedBranch
    var header by remember { mutableStateOf(selectedBranch.receiptHeader) }
    var subtitle by remember { mutableStateOf(selectedBranch.receiptSubtitle) }
    var vatTin by remember { mutableStateOf(selectedBranch.receiptVatTin) }
    var address by remember { mutableStateOf(selectedBranch.address) }
    var phone by remember { mutableStateOf(selectedBranch.phone) }
    var footer by remember { mutableStateOf(selectedBranch.receiptFooter) }
    var taxPercentText by remember { mutableStateOf(String.format(Locale.US, "%.1f", selectedBranch.taxPercent).removeSuffix(".0")) }
    var receiptGap by remember { mutableIntStateOf(selectedBranch.receiptGap) }
    var showPreview by remember { mutableStateOf(true) }
    var setAsActiveBranch by remember { mutableStateOf(selectedBranch.id == activeBranch?.id) }

    // Sync form when branch selection changes
    LaunchedEffect(selectedBranch.id) {
        header = selectedBranch.receiptHeader
        subtitle = selectedBranch.receiptSubtitle
        vatTin = selectedBranch.receiptVatTin
        address = selectedBranch.address
        phone = selectedBranch.phone
        footer = selectedBranch.receiptFooter
        taxPercentText = String.format(Locale.US, "%.1f", selectedBranch.taxPercent).removeSuffix(".0")
        receiptGap = selectedBranch.receiptGap
        setAsActiveBranch = selectedBranch.id == activeBranch?.id
    }

    val parsedTaxPercent = taxPercentText.toDoubleOrNull() ?: selectedBranch.taxPercent

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 760.dp)
                .padding(vertical = 16.dp)
                .testTag("branch_receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RetailTealPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = RetailTealPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Branch Receipt & Tax Settings",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "Customize thermal receipt header, tax rate, and spacing by branch",
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                HorizontalDivider(color = RetailSlate100)

                // Branch Selector Tabs
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SELECT BRANCH TO CONFIGURE:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailSlate500,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        branches.forEach { branch ->
                            val isSelected = branch.id == selectedBranch.id
                            val isActive = branch.id == activeBranch?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBranch = branch },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(branch.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else RetailTealPrimary)
                                                    .size(6.dp)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RetailTealPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Branch Status & Quick Activate Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Branch: ${selectedBranch.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RetailSlate900
                            )
                            if (selectedBranch.isMain) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RetailTealPrimary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "HEADQUARTERS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RetailTealPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (selectedBranch.id != activeBranch?.id) {
                            OutlinedButton(
                                onClick = {
                                    onSetActiveBranch(selectedBranch)
                                    setAsActiveBranch = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set as Active POS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Active in POS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Section 1: Store & Header Customization
                Text(
                    text = "1. Store Header & Branding (Printed on Receipt)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = header,
                    onValueChange = { header = it },
                    label = { Text("Store / Header Name on Receipt") },
                    placeholder = { Text("e.g. TR COFFEE • Downtown Branch (កាហ្វេ ទីរ៉ូ)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("receipt_header_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Receipt Subtitle / Tagline") },
                        placeholder = { Text("Official Sales Receipt & Tax Invoice") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("receipt_subtitle_input")
                    )

                    OutlinedTextField(
                        value = vatTin,
                        onValueChange = { vatTin = it },
                        label = { Text("VAT / TIN Number") },
                        placeholder = { Text("VAT TIN: K001-90213847") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("receipt_vattin_input")
                    )
                }

                // Section 2: Store Address & Phone
                Text(
                    text = "2. Branch Address & Phone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RetailSlate900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Physical Street Address") },
                        placeholder = { Text("123 Norodom Blvd, Daun Penh, Phnom Penh") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = RetailSlate500) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).testTag("receipt_address_input")
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Store Contact Number") },
                        placeholder = { Text("+855 23 888 999") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = RetailSlate500) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.8f).testTag("receipt_phone_input")
                    )
                }

                // Section 3: Tax Percentage Configuration ("edit tax percent")
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Percent, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3. Sales Tax Rate for this Branch (${parsedTaxTaxRateLabel(parsedTaxPercent)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                        }

                        Text(
                            text = "Select preset or enter custom percentage applied to orders at this branch:",
                            fontSize = 11.5.sp,
                            color = RetailSlate700
                        )

                        // Quick tax presets chips
                        val presets = listOf(0.0, 5.0, 7.0, 8.0, 10.0, 12.0)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { preset ->
                                val isSelected = (parsedTaxPercent == preset)
                                Surface(
                                    color = if (isSelected) Color(0xFF16A34A) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF16A34A) else Color(0xFF86EFAC)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            taxPercentText = if (preset % 1.0 == 0.0) preset.toInt().toString() else preset.toString()
                                        }
                                        .testTag("tax_preset_${preset.toInt()}")
                                ) {
                                    Text(
                                        text = if (preset == 0.0) "0% (Exempt)" else "${preset.toInt()}%",
                                        color = if (isSelected) Color.White else Color(0xFF166534),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Custom percentage input
                        OutlinedTextField(
                            value = taxPercentText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                    taxPercentText = input
                                }
                            },
                            label = { Text("Tax Percent (%)") },
                            placeholder = { Text("e.g. 8.0, 10.0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tax_percent_input")
                        )
                    }
                }

                // Section 4: Receipt Spacing Gap ("add gap for editing receipt")
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SpaceBar, contentDescription = null, tint = RetailSlate700, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "4. Thermal Receipt Spacing Gap ($receiptGap dp)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = RetailSlate900
                            )
                        }

                        Text(
                            text = "Adjust vertical spacing gap between receipt blocks (header, item lines, summary):",
                            fontSize = 11.5.sp,
                            color = RetailSlate500
                        )

                        val gaps = listOf(
                            6 to "Compact (6dp)",
                            12 to "Standard (12dp)",
                            18 to "Spacious (18dp)",
                            24 to "Wide (24dp)"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            gaps.forEach { (gapVal, label) ->
                                val isSelected = (receiptGap == gapVal)
                                Surface(
                                    color = if (isSelected) RetailTealPrimary else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) RetailTealPrimary else RetailSlate300
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { receiptGap = gapVal }
                                        .testTag("gap_preset_$gapVal")
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else RetailSlate800,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 5: Receipt Footer
                Text(
                    text = "5. Receipt Footer Note (Customer Greeting / Wi-Fi / Policy)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = { Text("Footer Message") },
                    placeholder = { Text("Thank you for shopping with us! • Goods returnable within 7 days") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("receipt_footer_input")
                )

                // Toggle Live Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPreview = !showPreview }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Visibility, contentDescription = null, tint = RetailTealPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Thermal Receipt Preview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = RetailTealDark
                        )
                    }
                    Text(
                        text = if (showPreview) "Hide Preview ▲" else "Show Preview ▼",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetailTealPrimary
                    )
                }

                AnimatedVisibility(visible = showPreview) {
                    ThermalReceiptPreviewCard(
                        header = header.ifBlank { "TR COFFEE • ${selectedBranch.name}" },
                        subtitle = subtitle.ifBlank { "Official Sales Receipt & Tax Invoice" },
                        address = address.ifBlank { "123 Norodom Blvd, Daun Penh, Phnom Penh" },
                        phone = phone.ifBlank { "+855 23 888 999" },
                        vatTin = vatTin.ifBlank { "VAT TIN: K001-90213847" },
                        footer = footer.ifBlank { "Thank you for shopping with us!" },
                        taxPercent = parsedTaxPercent,
                        receiptGap = receiptGap
                    )
                }

                HorizontalDivider(color = RetailSlate100)

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            header = "TR COFFEE • ${selectedBranch.name}"
                            subtitle = "Official Sales Receipt & Tax Invoice"
                            vatTin = "VAT TIN: K001-90213847"
                            footer = "Thank you for visiting TR Coffee! • Goods returnable within 7 days"
                            taxPercentText = "8.0"
                            receiptGap = 12
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Defaults", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveBranchReceipt(
                                    selectedBranch,
                                    header,
                                    subtitle,
                                    vatTin,
                                    address,
                                    phone,
                                    footer,
                                    parsedTaxPercent,
                                    receiptGap,
                                    setAsActiveBranch
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_receipt_config_btn")
                        ) {
                            Text("Save for ${selectedBranch.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Realistic thermal paper receipt preview showing header, live gap spacing, items,
 * and dynamic sales tax percentage.
 */
@Composable
private fun ThermalReceiptPreviewCard(
    header: String,
    subtitle: String,
    address: String,
    phone: String,
    vatTin: String,
    footer: String,
    taxPercent: Double,
    receiptGap: Int
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFFBEB), // Authentic thermal paper cream tint
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_receipt_preview")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = header,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF0F4D2A),
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = RetailSlate500,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$address • $phone",
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                color = RetailSlate500,
                textAlign = TextAlign.Center
            )
            if (vatTin.isNotBlank()) {
                Text(
                    text = vatTin,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = RetailSlate500
                )
            }

            // Gap spacing after header
            Spacer(modifier = Modifier.height(receiptGap.dp))

            // Metadata Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RECEIPT: #REC-PREVIEW", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("CASHIER: Sarah M.", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // Gap spacing before sample items
            Spacer(modifier = Modifier.height((receiptGap / 2).coerceAtLeast(4).dp))

            // Sample item list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                PreviewReceiptItemRow("Iced Caramel Latte (M)", "2x", "$3.50", "$7.00")
                PreviewReceiptItemRow("Glazed French Croissant", "1x", "$2.50", "$2.50")
            }

            // Gap spacing before summary
            Spacer(modifier = Modifier.height((receiptGap / 2).coerceAtLeast(4).dp))
            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(4.dp))

            // Summary Rows
            val subtotal = 9.50
            val taxAmount = subtotal * (taxPercent / 100.0)
            val total = subtotal + taxAmount

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SUBTOTAL", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = RetailSlate700)
                Text("$${String.format(Locale.US, "%.2f", subtotal)}", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "SALES TAX (${String.format(Locale.US, "%.1f", taxPercent).removeSuffix(".0")}%)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
                Text(
                    "$${String.format(Locale.US, "%.2f", taxAmount)}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL DUE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
                Text("$${String.format(Locale.US, "%.2f", total)}", fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetailTealPrimary)
            }

            // Gap spacing before footer
            Spacer(modifier = Modifier.height(receiptGap.dp))

            // Footer
            Text(
                text = footer,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                color = RetailSlate700,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "⚡ Official Tax Invoice Generated by TR Retail POS",
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                color = RetailSlate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PreviewReceiptItemRow(name: String, qty: String, price: String, total: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
            Text("$qty @ $price", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = RetailSlate500)
        }
        Text(total, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

private fun parsedTaxTaxRateLabel(taxPercent: Double): String {
    return if (taxPercent == 0.0) "0% Exempt" else "${String.format(Locale.US, "%.1f", taxPercent).removeSuffix(".0")}%"
}

/**
 * Quick Dialog for changing Sales Tax Percentage on the fly in the POS cart sheet.
 */
@Composable
fun EditTaxPercentDialog(
    currentTaxPercent: Double,
    cartSubtotal: Double,
    onDismiss: () -> Unit,
    onApplyTaxPercent: (newPercent: Double, updateBranchDefault: Boolean) -> Unit
) {
    var taxText by remember { mutableStateOf(String.format(Locale.US, "%.1f", currentTaxPercent).removeSuffix(".0")) }
    var updateBranchDefault by remember { mutableStateOf(false) }

    val parsedPercent = taxText.toDoubleOrNull() ?: currentTaxPercent
    val sampleTaxAmount = cartSubtotal * (parsedPercent / 100.0)
    val sampleTotal = cartSubtotal + sampleTaxAmount

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("edit_tax_percent_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Percent, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Edit Sales Tax Rate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = RetailSlate900
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                Text(
                    text = "Adjust tax percent for this transaction or save as branch default:",
                    fontSize = 12.sp,
                    color = RetailSlate500
                )

                // Quick presets
                val presets = listOf(0.0, 5.0, 7.0, 8.0, 10.0, 12.0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = (parsedPercent == preset)
                        Surface(
                            color = if (isSelected) Color(0xFF16A34A) else RetailSlate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable {
                                    taxText = if (preset % 1.0 == 0.0) preset.toInt().toString() else preset.toString()
                                }
                                .testTag("quick_tax_${preset.toInt()}")
                        ) {
                            Text(
                                text = if (preset == 0.0) "0% (Exempt)" else "${preset.toInt()}%",
                                color = if (isSelected) Color.White else RetailSlate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = taxText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                            taxText = input
                        }
                    },
                    label = { Text("Tax Percent (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("pos_tax_percent_input")
                )

                // Live Impact Preview
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Cart Subtotal:", fontSize = 11.sp, color = RetailSlate500)
                            Text("$${String.format(Locale.US, "%.2f", cartSubtotal)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Calculated Tax (${parsedPercent}%):", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                            Text("$${String.format(Locale.US, "%.2f", sampleTaxAmount)}", fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimated Total Due:", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Text("$${String.format(Locale.US, "%.2f", sampleTotal)}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = RetailTealPrimary)
                        }
                    }
                }

                // Checkbox to update branch default
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { updateBranchDefault = !updateBranchDefault }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = updateBranchDefault,
                        onCheckedChange = { updateBranchDefault = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save as default tax rate for active branch",
                        fontSize = 12.sp,
                        color = RetailSlate800
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApplyTaxPercent(parsedPercent, updateBranchDefault)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("apply_tax_percent_btn")
                    ) {
                        Text("Apply Tax")
                    }
                }
            }
        }
    }
}
