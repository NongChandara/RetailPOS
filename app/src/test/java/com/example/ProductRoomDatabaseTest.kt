package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.dao.ProductDao
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductRoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndReadRetailProductWithAllRequiredFields() = runBlocking {
        // Retail product with name, price, stock quantity, and category
        val product = ProductEntity(
            name = "Organic Cold Brew Coffee",
            price = 4.75,
            stockQuantity = 45,
            category = "Beverages"
        )

        val insertedId = productDao.insertProduct(product)
        assertTrue("Inserted product should have valid ID", insertedId > 0)

        val retrieved = productDao.getProductById(insertedId)
        assertNotNull("Product should be retrieved from Room DB", retrieved)
        assertEquals("Organic Cold Brew Coffee", retrieved?.name)
        assertEquals(4.75, retrieved?.price ?: 0.0, 0.001)
        assertEquals(4.75, retrieved?.sellingPrice ?: 0.0, 0.001)
        assertEquals(45, retrieved?.stockQuantity)
        assertEquals("Beverages", retrieved?.category)
    }

    @Test
    fun queryProductsByCategory() = runBlocking {
        val prod1 = ProductEntity(name = "Green Tea", price = 3.50, stockQuantity = 30, category = "Beverages")
        val prod2 = ProductEntity(name = "Espresso Roast", price = 12.00, stockQuantity = 15, category = "Beverages")
        val prod3 = ProductEntity(name = "Croissant", price = 2.75, stockQuantity = 20, category = "Bakery")
        val prod4 = ProductEntity(name = "Chocolate Donut", price = 1.95, stockQuantity = 25, category = "Bakery")

        productDao.insertProduct(prod1)
        productDao.insertProduct(prod2)
        productDao.insertProduct(prod3)
        productDao.insertProduct(prod4)

        val beverages = productDao.getProductsByCategory("Beverages").first()
        assertEquals(2, beverages.size)
        assertTrue(beverages.all { it.category == "Beverages" })

        val bakery = productDao.getProductsByCategory("Bakery").first()
        assertEquals(2, bakery.size)
        assertTrue(bakery.all { it.category == "Bakery" })

        val allCategories = productDao.getAllCategories().first()
        assertTrue(allCategories.contains("Beverages"))
        assertTrue(allCategories.contains("Bakery"))
    }

    @Test
    fun updateProductPriceAndStockQuantity() = runBlocking {
        val product = ProductEntity(
            name = "Handcrafted Ceramic Mug",
            price = 15.00,
            stockQuantity = 10,
            category = "Merchandise"
        )
        val id = productDao.insertProduct(product)

        val existing = productDao.getProductById(id)
        assertNotNull(existing)

        // Update price and stock quantity
        val updated = existing!!.copy(
            sellingPrice = 18.50,
            stockQuantity = 8
        )
        productDao.updateProduct(updated)

        val reloaded = productDao.getProductById(id)
        assertNotNull(reloaded)
        assertEquals("Handcrafted Ceramic Mug", reloaded?.name)
        assertEquals(18.50, reloaded?.price ?: 0.0, 0.001)
        assertEquals(8, reloaded?.stockQuantity)
        assertEquals("Merchandise", reloaded?.category)
    }

    @Test
    fun deleteProductFromRoomDatabase() = runBlocking {
        val product = ProductEntity(
            name = "Seasonal Pumpkin Spice Syrup",
            price = 6.25,
            stockQuantity = 5,
            category = "Beverages"
        )
        val id = productDao.insertProduct(product)
        val retrieved = productDao.getProductById(id)
        assertNotNull(retrieved)

        productDao.deleteProduct(retrieved!!)
        val afterDeletion = productDao.getProductById(id)
        assertNull("Product should be null after deletion", afterDeletion)
    }

    @Test
    fun testStockAdjustmentAndInventoryThresholds() = runBlocking {
        val product = ProductEntity(
            name = "Matcha Powder 100g",
            sku = "TEA-MAT-100",
            category = "Beverages",
            costPrice = 8.00,
            sellingPrice = 16.00,
            stockQuantity = 6,
            minStockThreshold = 5,
            barcode = "885123456789"
        )
        val id = productDao.insertProduct(product)

        // Currently 6 items in stock (above minStockThreshold of 5)
        val lowStockInitial = productDao.getLowStockProducts().first()
        assertTrue(lowStockInitial.none { it.id == id })

        // Adjust stock down by 2 (resulting in 4, which is <= 5)
        productDao.adjustStock(id, -2)
        val lowStockAfter = productDao.getLowStockProducts().first()
        assertTrue("Product should be in low stock list when quantity <= threshold", lowStockAfter.any { it.id == id })
        val updatedProd = productDao.getProductById(id)
        assertEquals(4, updatedProd?.stockQuantity)

        // Lookup by barcode
        val byBarcode = productDao.getProductByBarcode("885123456789")
        assertNotNull("Should find product by barcode", byBarcode)
        assertEquals(id, byBarcode?.id)

        // Adjust down to 0
        productDao.adjustStock(id, -10) // MAX(0, stockQuantity - 10) ensures non-negative
        val outOfStockList = productDao.getOutOfStockProducts().first()
        assertTrue("Product should be in out-of-stock list", outOfStockList.any { it.id == id })

        // Delete by ID
        productDao.deleteProductById(id)
        assertNull(productDao.getProductById(id))
    }
}
