package com.alafreet.dokan.data

import com.alafreet.dokan.data.dao.BestSellerRow
import com.alafreet.dokan.data.entities.Category
import com.alafreet.dokan.data.entities.PaymentMethod
import com.alafreet.dokan.data.entities.Product
import com.alafreet.dokan.data.entities.Sale
import com.alafreet.dokan.data.entities.SaleItem
import kotlinx.coroutines.flow.Flow

data class CartLine(
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double get() = product.finalPrice * quantity
}

class DokanRepository(private val db: AppDatabase) {

    fun getActiveProducts(): Flow<List<Product>> = db.productDao().getAllActiveProducts()
    fun getAllProducts(): Flow<List<Product>> = db.productDao().getAllProducts()
    fun searchProducts(query: String): Flow<List<Product>> = db.productDao().searchProducts(query)
    fun getLowStockProducts(): Flow<List<Product>> = db.productDao().getLowStockProducts()

    suspend fun addProduct(product: Product): Long = db.productDao().insert(product)
    suspend fun updateProduct(product: Product) = db.productDao().update(product)
    suspend fun deleteProduct(product: Product) = db.productDao().delete(product)
    suspend fun adjustStock(productId: Long, delta: Int) {
        if (delta >= 0) db.productDao().increaseStock(productId, delta)
        else db.productDao().decreaseStock(productId, -delta)
    }

    fun getCategories(): Flow<List<Category>> = db.categoryDao().getAllCategories()
    suspend fun addCategory(category: Category): Long = db.categoryDao().insert(category)
    suspend fun deleteCategory(category: Category) = db.categoryDao().delete(category)

    fun getAllSales(): Flow<List<Sale>> = db.saleDao().getAllSales()
    fun getRevenueBetween(start: Long, end: Long): Flow<Double> = db.saleDao().getTotalRevenueBetween(start, end)
    fun getTransactionCountBetween(start: Long, end: Long): Flow<Int> = db.saleDao().getTransactionCountBetween(start, end)
    fun getBestSellers(start: Long, end: Long, limit: Int = 5): Flow<List<BestSellerRow>> =
        db.saleDao().getBestSellers(start, end, limit)

    companion object {
        const val TAX_RATE = 0.15
    }

    suspend fun completeSale(cart: List<CartLine>, paymentMethod: PaymentMethod): Long {
        require(cart.isNotEmpty()) { "لا يمكن إتمام بيع بسلة فارغة" }

        val subtotal = cart.sumOf { it.product.price * it.quantity }
        val discountAmount = cart.sumOf { (it.product.price - it.product.finalPrice) * it.quantity }
        val taxableAmount = subtotal - discountAmount
        val taxAmount = taxableAmount * TAX_RATE
        val total = taxableAmount + taxAmount

        val sale = Sale(
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            total = total,
            paymentMethod = paymentMethod
        )

        val saleItems = cart.map { line ->
            SaleItem(
                saleId = 0,
                productId = line.product.id,
                productNameSnapshot = line.product.name,
                unitPriceSnapshot = line.product.finalPrice,
                quantity = line.quantity,
                lineTotal = line.lineTotal
            )
        }

        val saleId = db.saleDao().insertSaleWithItems(sale, saleItems)

        cart.forEach { line ->
            db.productDao().decreaseStock(line.product.id, line.quantity)
        }

        return saleId
    }
}
