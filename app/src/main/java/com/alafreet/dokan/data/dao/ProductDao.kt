package com.alafreet.dokan.data.dao

import androidx.room.*
import com.alafreet.dokan.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE quantity <= minStockThreshold AND isActive = 1 ORDER BY quantity ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("""
        SELECT * FROM products 
        WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR barcode = :query)
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("UPDATE products SET quantity = quantity - :amount WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, amount: Int)

    @Query("UPDATE products SET quantity = quantity + :amount WHERE id = :productId")
    suspend fun increaseStock(productId: Long, amount: Int)
}
