package com.example.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BranchEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.BranchReceiptAndTaxDialog
import com.example.ui.theme.RetailSlate100
import com.example.ui.theme.RetailSlate300
import com.example.ui.theme.RetailSlate500
import com.example.ui.theme.RetailSlate700
import com.example.ui.theme.RetailSlate900
import com.example.ui.theme.RetailStatusAmber
import com.example.ui.theme.RetailStatusGreen
import com.example.ui.theme.RetailStatusRed
import com.example.ui.theme.RetailTealPrimary
import java.util.Locale

private val PRESET_COLORS = listOf(
    "#0D9488", // Teal
    "#0284C7", // Sky blue
    "#D97706", // Amber
    "#16A34A", // Green
    "#7C3AED", // Purple
    "#DB2777", // Rose
    "#EA580C", // Orange
    "#4B5563"  // Slate
)

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        RetailTealPrimary
    }
}

@Composable
fun ManageCategoriesDialog(
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, description: String, colorHex: String) -> Unit,
    onEditCategory: (category: CategoryEntity, newName: String, newDescription: String, newColorHex: String) -> Unit,
    onDeleteCategory: (category: CategoryEntity) -> Unit
) {
    var newCatName by remember { mutableStateOf("") }
    var newCatDesc by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PRESET_COLORS.first()) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RetailTealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Category,
                                    contentDescription = null,
                                    tint = RetailTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Manage Categories",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "${categories.size} categories available",
                                fontSize = 12.sp,
                                color = RetailSlate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = RetailSlate100)

                // Add New Category Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Add New Category",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = RetailSlate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                label = { Text("Category Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_category_name_input")
                            )
                            Button(
                                onClick = {
                                    if (newCatName.isNotBlank()) {
                                        onAddCategory(newCatName.trim(), newCatDesc.trim(), selectedColor)
                                        newCatName = ""
                                        newCatDesc = ""
                                    }
                                },
                                enabled = newCatName.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .testTag("add_category_confirm_btn")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newCatDesc,
                            onValueChange = { newCatDesc = it },
                            label = { Text("Optional description") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Color swatches
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Color:", fontSize = 11.sp, color = RetailSlate500)
                            PRESET_COLORS.forEach { colorHex ->
                                val color = parseColor(colorHex)
                                val isSelected = selectedColor.equals(colorHex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColor = colorHex }
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) RetailSlate900 else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Existing Categories",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RetailSlate700
                )
                Spacer(modifier = Modifier.height(6.dp))

                // List of Categories
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        val productCount = products.count { it.category.equals(cat.name, ignoreCase = true) }
                        CategoryRowItem(
                            category = cat,
                            productCount = productCount,
                            onEdit = { categoryToEdit = cat },
                            onDelete = { categoryToDelete = cat }
                        )
                    }
                }
            }
        }
    }

    // Edit Category Dialog
    categoryToEdit?.let { cat ->
        EditCategoryDialog(
            category = cat,
            onDismiss = { categoryToEdit = null },
            onSave = { updatedName, updatedDesc, updatedColor ->
                onEditCategory(cat, updatedName, updatedDesc, updatedColor)
                categoryToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        val count = products.count { it.category.equals(cat.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category '${cat.name}'?") },
            text = {
                Text(
                    if (count > 0) {
                        "There are $count products assigned to '${cat.name}'. If deleted, these products will be moved to the 'General' category."
                    } else {
                        "Are you sure you want to delete category '${cat.name}'?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(cat)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RetailStatusRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryRowItem(
    category: CategoryEntity,
    productCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate100),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(parseColor(category.colorHex))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RetailSlate900
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = RetailSlate100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$productCount products",
                                fontSize = 10.sp,
                                color = RetailSlate700,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (category.description.isNotBlank()) {
                        Text(
                            text = category.description,
                            fontSize = 11.sp,
                            color = RetailSlate500,
                            maxLines = 1
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_category_${category.id}")
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Category",
                        tint = RetailTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_category_${category.id}")
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Category",
                        tint = RetailStatusRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (newName: String, newDesc: String, newColor: String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var desc by remember { mutableStateOf(category.description) }
    var colorHex by remember { mutableStateOf(category.colorHex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Edit Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_category_name_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Theme Color", fontSize = 12.sp, color = RetailSlate500)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_COLORS.forEach { hex ->
                        val color = parseColor(hex)
                        val isSelected = colorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorHex = hex }
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) RetailSlate900 else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), desc.trim(), colorHex)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_category_changes_btn")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun ManageBranchesDialog(
    branches: List<BranchEntity>,
    products: List<ProductEntity>,
    activeBranch: BranchEntity? = null,
    onDismiss: () -> Unit,
    onAddBranch: (name: String, code: String, address: String, phone: String, isMain: Boolean) -> Unit,
    onEditBranch: (branch: BranchEntity, newName: String, newCode: String, newAddress: String, newPhone: String, isMain: Boolean) -> Unit,
    onDeleteBranch: (branch: BranchEntity) -> Unit,
    onUpdateBranchReceiptConfig: ((
        branch: BranchEntity,
        receiptHeader: String,
        receiptSubtitle: String,
        receiptVatTin: String,
        address: String,
        phone: String,
        receiptFooter: String,
        taxPercent: Double,
        receiptGap: Int,
        setAsActive: Boolean
    ) -> Unit)? = null,
    onSetActiveBranch: ((BranchEntity) -> Unit)? = null
) {
    var newBranchName by remember { mutableStateOf("") }
    var newBranchCode by remember { mutableStateOf("") }
    var newBranchAddress by remember { mutableStateOf("") }
    var newBranchPhone by remember { mutableStateOf("") }
    var branchToEdit by remember { mutableStateOf<BranchEntity?>(null) }
    var branchToDelete by remember { mutableStateOf<BranchEntity?>(null) }
    var branchForReceiptConfig by remember { mutableStateOf<BranchEntity?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RetailTealPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Storefront,
                                    contentDescription = null,
                                    tint = RetailTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Branch Management",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RetailSlate900
                            )
                            Text(
                                text = "${branches.size} active store locations • Edit receipt & tax per branch",
                                fontSize = 11.5.sp,
                                color = RetailSlate500
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = RetailSlate500)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = RetailSlate100)

                // Add Branch Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = RetailSlate100.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Add New Branch",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = RetailSlate900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newBranchName,
                                onValueChange = { newBranchName = it },
                                label = { Text("Branch Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("new_branch_name_input")
                            )
                            OutlinedTextField(
                                value = newBranchCode,
                                onValueChange = { newBranchCode = it },
                                label = { Text("Code (e.g. BR-04)") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newBranchAddress,
                                onValueChange = { newBranchAddress = it },
                                label = { Text("Street Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newBranchPhone,
                                onValueChange = { newBranchPhone = it },
                                label = { Text("Phone") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newBranchName.isNotBlank()) {
                                    onAddBranch(
                                        newBranchName.trim(),
                                        newBranchCode.trim(),
                                        newBranchAddress.trim(),
                                        newBranchPhone.trim(),
                                        false
                                    )
                                    newBranchName = ""
                                    newBranchCode = ""
                                    newBranchAddress = ""
                                    newBranchPhone = ""
                                }
                            },
                            enabled = newBranchName.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_branch_confirm_btn")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Branch")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Branches",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RetailSlate700
                    )
                    Text(
                        text = "Click ⚙ to edit receipt & tax",
                        fontSize = 11.sp,
                        color = RetailTealPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // List of Branches
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(branches, key = { it.id }) { branch ->
                        val count = products.count { it.branch.equals(branch.name, ignoreCase = true) }
                        val isActive = branch.id == activeBranch?.id
                        BranchRowItem(
                            branch = branch,
                            productCount = count,
                            isActivePos = isActive,
                            onEdit = { branchToEdit = branch },
                            onCustomizeReceipt = { branchForReceiptConfig = branch },
                            onDelete = { branchToDelete = branch }
                        )
                    }
                }
            }
        }
    }

    // Branch Receipt & Tax Customization Dialog
    branchForReceiptConfig?.let { targetBranch ->
        BranchReceiptAndTaxDialog(
            branches = branches,
            activeBranch = activeBranch,
            initialBranch = targetBranch,
            onDismiss = { branchForReceiptConfig = null },
            onSaveBranchReceipt = { branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive ->
                onUpdateBranchReceiptConfig?.invoke(branch, header, subtitle, vatTin, addr, phone, footer, taxPct, gap, setAsActive)
                branchForReceiptConfig = null
            },
            onSetActiveBranch = { b ->
                onSetActiveBranch?.invoke(b)
            }
        )
    }

    // Edit Branch Dialog
    branchToEdit?.let { branch ->
        EditBranchDialog(
            branch = branch,
            onDismiss = { branchToEdit = null },
            onSave = { newName, newCode, newAddr, newPhone, isMain ->
                onEditBranch(branch, newName, newCode, newAddr, newPhone, isMain)
                branchToEdit = null
            },
            onSaveWithReceipt = { b, newName, newCode, newAddr, newPhone, isMain, header, subtitle, vatTin, footer, taxPct, gap ->
                onEditBranch(b, newName, newCode, newAddr, newPhone, isMain)
                onUpdateBranchReceiptConfig?.invoke(b, header, subtitle, vatTin, newAddr, newPhone, footer, taxPct, gap, false)
                branchToEdit = null
            }
        )
    }

    // Delete Branch Alert
    branchToDelete?.let { branch ->
        val count = products.count { it.branch.equals(branch.name, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { branchToDelete = null },
            title = { Text("Delete Branch '${branch.name}'?") },
            text = {
                Text(
                    if (count > 0) {
                        "There are $count products assigned to '${branch.name}'. If deleted, all inventory will be reassigned to 'Main Branch'."
                    } else {
                        "Are you sure you want to delete branch '${branch.name}'?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBranch(branch)
                        branchToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RetailStatusRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { branchToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BranchRowItem(
    branch: BranchEntity,
    productCount: Int,
    isActivePos: Boolean = false,
    onEdit: () -> Unit,
    onCustomizeReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RetailSlate100),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = branch.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = RetailSlate900
                    )
                    if (branch.isMain) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = RetailTealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "MAIN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = RetailTealPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isActivePos) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ACTIVE POS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (branch.code.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = RetailSlate100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = branch.code,
                                fontSize = 10.sp,
                                color = RetailSlate700,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (branch.address.isNotBlank()) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = RetailSlate500, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(branch.address, fontSize = 11.sp, color = RetailSlate500)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("$productCount items", fontSize = 11.sp, color = RetailTealPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(3.dp))
                // Receipt & Tax Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFBBF7D0))
                    ) {
                        Text(
                            text = "Tax: ${String.format(Locale.US, "%.1f", branch.taxPercent).removeSuffix(".0")}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Surface(
                        color = RetailSlate100,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Gap: ${branch.receiptGap}dp",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = RetailSlate700,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Customize Receipt Button
                IconButton(
                    onClick = onCustomizeReceipt,
                    modifier = Modifier.size(32.dp).testTag("receipt_branch_${branch.id}")
                ) {
                    Icon(
                        Icons.Filled.ReceiptLong,
                        contentDescription = "Customize Receipt & Tax",
                        tint = RetailTealPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp).testTag("edit_branch_${branch.id}")
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Branch",
                        tint = RetailTealPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                if (!branch.isMain) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_branch_${branch.id}")
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Branch",
                            tint = RetailStatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditBranchDialog(
    branch: BranchEntity,
    onDismiss: () -> Unit,
    onSave: (newName: String, newCode: String, newAddress: String, newPhone: String, isMain: Boolean) -> Unit,
    onSaveWithReceipt: ((
        branch: BranchEntity,
        newName: String,
        newCode: String,
        newAddress: String,
        newPhone: String,
        isMain: Boolean,
        header: String,
        subtitle: String,
        vatTin: String,
        footer: String,
        taxPercent: Double,
        gap: Int
    ) -> Unit)? = null
) {
    var name by remember { mutableStateOf(branch.name) }
    var code by remember { mutableStateOf(branch.code) }
    var address by remember { mutableStateOf(branch.address) }
    var phone by remember { mutableStateOf(branch.phone) }
    var isMain by remember { mutableStateOf(branch.isMain) }
    var receiptHeader by remember { mutableStateOf(branch.receiptHeader) }
    var receiptSubtitle by remember { mutableStateOf(branch.receiptSubtitle) }
    var receiptVatTin by remember { mutableStateOf(branch.receiptVatTin) }
    var receiptFooter by remember { mutableStateOf(branch.receiptFooter) }
    var taxPercentText by remember { mutableStateOf(String.format(Locale.US, "%.1f", branch.taxPercent).removeSuffix(".0")) }
    var receiptGap by remember { mutableIntStateOf(branch.receiptGap) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Edit Branch & Receipt Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Branch Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_branch_name_input")
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Branch Code (e.g. MB-01)") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Street Address") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = RetailSlate100, modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Receipt & Tax Settings for this Branch",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = RetailSlate900
                )

                OutlinedTextField(
                    value = receiptHeader,
                    onValueChange = { receiptHeader = it },
                    label = { Text("Receipt Header (Printed on Receipt)") },
                    placeholder = { Text("e.g. TR COFFEE • ${branch.name}") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = taxPercentText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                taxPercentText = input
                            }
                        },
                        label = { Text("Tax Percent (%)") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("edit_branch_tax_percent_input")
                    )

                    // Gap selector chips
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Spacing Gap", fontSize = 11.sp, color = RetailSlate500)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(6, 12, 18, 24).forEach { g ->
                                Surface(
                                    color = if (receiptGap == g) RetailTealPrimary else RetailSlate100,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable { receiptGap = g }
                                        .testTag("edit_branch_gap_$g")
                                ) {
                                    Text(
                                        text = "${g}p",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (receiptGap == g) Color.White else RetailSlate900,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = receiptFooter,
                    onValueChange = { receiptFooter = it },
                    label = { Text("Receipt Footer Note") },
                    placeholder = { Text("Thank you for visiting! • Goods returnable in 7 days") },
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val parsedTax = taxPercentText.toDoubleOrNull() ?: branch.taxPercent
                                if (onSaveWithReceipt != null) {
                                    onSaveWithReceipt(
                                        branch,
                                        name.trim(),
                                        code.trim(),
                                        address.trim(),
                                        phone.trim(),
                                        isMain,
                                        receiptHeader.trim().ifBlank { "TR COFFEE • ${name.trim()}" },
                                        receiptSubtitle.trim(),
                                        receiptVatTin.trim(),
                                        receiptFooter.trim(),
                                        parsedTax,
                                        receiptGap
                                    )
                                } else {
                                    onSave(name.trim(), code.trim(), address.trim(), phone.trim(), isMain)
                                }
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = RetailTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_branch_changes_btn")
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
