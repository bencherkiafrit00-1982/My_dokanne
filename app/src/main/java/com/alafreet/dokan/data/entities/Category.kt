package com.alafreet.dokan.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * تصنيف المنتجات (مثال: مشروبات، مواد غذائية، أدوات منزلية...)
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#6750A4"
)
