package com.alafreet.dokan.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMethod {
    CASH, CARD, WALLET
}

/**
 * كيان عملية بيع مكتملة (فاتورة)
 */
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double = 0.0,
    val total: Double,
    val paymentMethod: PaymentMethod
)
