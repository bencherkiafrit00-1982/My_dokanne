package com.alafreet.dokan.utils

import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Category
import com.alafreet.dokan.data.entities.Product
import kotlinx.coroutines.flow.first

suspend fun seedSampleDataIfEmpty(repository: DokanRepository) {
    val existingProducts = repository.getAllProducts().first()
    if (existingProducts.isNotEmpty()) return

    val groceriesId = repository.addCategory(Category(name = "مواد غذائية"))
    val drinksId = repository.addCategory(Category(name = "مشروبات"))
    val householdId = repository.addCategory(Category(name = "أدوات منزلية"))

    repository.addProduct(Product(name = "أرز مصري 1 كجم", price = 35.0, quantity = 40, minStockThreshold = 10, categoryId = groceriesId))
    repository.addProduct(Product(name = "زيت طبخ 1 لتر", price = 65.0, discountPercent = 5.0, quantity = 25, minStockThreshold = 8, categoryId = groceriesId))
    repository.addProduct(Product(name = "سكر 1 كجم", price = 30.0, quantity = 3, minStockThreshold = 10, categoryId = groceriesId))
    repository.addProduct(Product(name = "مياه معدنية 1.5 لتر", price = 10.0, quantity = 60, minStockThreshold = 15, categoryId = drinksId))
    repository.addProduct(Product(name = "عصير مانجو", price = 15.0, quantity = 30, minStockThreshold = 10, categoryId = drinksId))
    repository.addProduct(Product(name = "منظف أطباق", price = 22.0, discountPercent = 10.0, quantity = 18, minStockThreshold = 5, categoryId = householdId))
}
