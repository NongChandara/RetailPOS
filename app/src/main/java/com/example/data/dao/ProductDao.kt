package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for handling local product inventory storage.
 * Provides reactive Flow streams and coroutine-based CRUD operations.
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= minStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stockQuantity <= 0 ORDER BY name ASC")
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku OR barcode = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("UPDATE products SET category = :newCategory WHERE category = :oldCategory")
    suspend fun updateCategoryName(oldCategory: String, newCategory: String)

    @Query("UPDATE products SET category = :fallbackCategory WHERE category = :categoryToDelete")
    suspend fun reassignCategory(categoryToDelete: String, fallbackCategory: String = "General")

    @Query("SELECT COUNT(*) FROM products WHERE category = :category")
    suspend fun countProductsInCategory(category: String): Int

    @Query("SELECT DISTINCT branch FROM products ORDER BY branch ASC")
    fun getAllBranchesFromProducts(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE branch = :branch ORDER BY name ASC")
    fun getProductsByBranch(branch: String): Flow<List<ProductEntity>>

    @Query("UPDATE products SET branch = :newBranch WHERE branch = :oldBranch")
    suspend fun updateBranchName(oldBranch: String, newBranch: String)

    @Query("UPDATE products SET branch = :fallbackBranch WHERE branch = :branchToDelete")
    suspend fun reassignBranch(branchToDelete: String, fallbackBranch: String = "Main Branch")

    @Query("SELECT COUNT(*) FROM products WHERE branch = :branch")
    suspend fun countProductsInBranch(branch: String): Int

    @Query("SELECT COUNT(*) FROM products")
    suspend fun countProducts(): Int

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE stockQuantity <= minStockThreshold")
    fun getLowStockCountFlow(): Flow<Int>

    @Query("SELECT SUM(costPrice * stockQuantity) FROM products")
    fun getTotalInventoryCostValue(): Flow<Double?>

    @Query("SELECT SUM(sellingPrice * stockQuantity) FROM products")
    fun getTotalInventoryRetailValue(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQuantity = MAX(0, stockQuantity + :quantityDelta), updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustStock(id: Long, quantityDelta: Int, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
