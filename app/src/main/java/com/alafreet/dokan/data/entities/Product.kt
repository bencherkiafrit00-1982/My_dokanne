package com.alafreet.dokan.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان المنتج - يمثل صنفاً معروضاً للبيع في دكان العفريت
 *
 * @param price سعر البيع الأساسي
 * @param discountPercent نسبة الخصم المطبقة على المنتج (0-100)
 * @param quantity الكمية المتوفرة حالياً في المخزون
 * @param minStockThreshold الحد الأدنى الذي عند الوصول إليه يتم إطلاق تنبيه نقص المخزون
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("barcode")]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val discountPercent: Double = 0.0,
    val quantity: Int,
    val minStockThreshold: Int = 5,
    val categoryId: Long? = null,
    val barcode: String? = null,
    val isActive: Boolean = true
) {
    /** السعر بعد تطبيق الخصم */
    val finalPrice: Double
        get() = price - (price * discountPercent / 100.0)
}
