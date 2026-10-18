package com.alafreet.dokan.data.dao

import androidx.room.*
import com.alafreet.dokan.data.entities.Sale
import com.alafreet.dokan.data.entities.SaleItem
import kotlinx.coroutines.flow.Flow

data class BestSellerRow(
    val productNameSnapshot: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Transaction
    suspend fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithSaleId)
        return saleId
    }

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItem>

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getSalesBetween(start: Long, end: Long): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sales WHERE timestamp BETWEEN :start AND :end")
    fun getTotalRevenueBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp BETWEEN :start AND :end")
    fun getTransactionCountBetween(start: Long, end: Long): Flow<Int>

    @Query("""
        SELECT productNameSnapshot, SUM(quantity) as totalQuantity, SUM(lineTotal) as totalRevenue
        FROM sale_items
        INNER JOIN sales ON sales.id = sale_items.saleId
        WHERE sales.timestamp BETWEEN :start AND :end
        GROUP BY productNameSnapshot
        ORDER BY totalQuantity DESC
        LIMIT :limit
    """)
    fun getBestSellers(start: Long, end: Long, limit: Int = 5): Flow<List<BestSellerRow>>
}
