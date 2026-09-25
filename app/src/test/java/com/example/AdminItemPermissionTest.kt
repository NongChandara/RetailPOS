package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import com.example.ui.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdminItemPermissionTest {

    private lateinit var app: Application
    private lateinit var viewModel: MainViewModel

    private val staffUser = UserEntity(
        id = 101L,
        name = "Cashier Sokha",
        role = "STAFF",
        pin = "1234"
    )

    private val adminUser = UserEntity(
        id = 102L,
        name = "Manager Vuthy",
        role = "ADMIN",
        pin = "9999"
    )

    private val testProduct = ProductEntity(
        id = 999L,
        name = "Specialty Espresso Beans",
        category = "Coffee Beans",
        sellingPrice = 14.50,
        costPrice = 9.00,
        stockQuantity = 20,
        minStockThreshold = 5,
        sku = "TEST-ESP-01"
    )

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(app)
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun nonAdminUserCannotEditProduct() = runTest {
        viewModel.setCurrentUser(staffUser)
        ShadowLooper.idleMainLooper()

        // Attempt to update product as staff
        viewModel.updateProduct(testProduct.copy(name = "Tampered Beans"))
        ShadowLooper.idleMainLooper()

        val toast = viewModel.toastMessage.value
        assertEquals("Action restricted: Only Admin users can edit items", toast)
    }

    @Test
    fun nonAdminUserCannotDeleteProduct() = runTest {
        viewModel.setCurrentUser(staffUser)
        ShadowLooper.idleMainLooper()

        // Attempt to delete product as staff
        viewModel.deleteProduct(testProduct)
        ShadowLooper.idleMainLooper()

        val toast = viewModel.toastMessage.value
        assertEquals("Action restricted: Only Admin users can delete items", toast)
    }

    @Test
    fun adminUserCanEditProduct() = runTest {
        viewModel.setCurrentUser(adminUser)
        ShadowLooper.idleMainLooper()

        viewModel.updateProduct(testProduct.copy(name = "Premium Beans"))

        repeat(20) {
            ShadowLooper.idleMainLooper()
            if (viewModel.toastMessage.value?.contains("updated") == true) return@runTest
            Thread.sleep(50)
        }

        val toast = viewModel.toastMessage.value
        println("DEBUG_EDIT_TOAST: $toast")
        assertTrue("Admin update should succeed, but got: $toast", toast?.contains("updated") == true)
    }

    @Test
    fun adminUserCanDeleteProduct() = runTest {
        viewModel.setCurrentUser(adminUser)
        ShadowLooper.idleMainLooper()

        viewModel.deleteProduct(testProduct)

        repeat(30) {
            ShadowLooper.idleMainLooper()
            if (viewModel.toastMessage.value?.contains("removed") == true) return@runTest
            Thread.sleep(100)
        }

        val toast = viewModel.toastMessage.value
        println("DEBUG_DELETE_TOAST: $toast")
        assertTrue("Admin delete should succeed, but got: $toast", toast?.contains("removed") == true)
    }
}
