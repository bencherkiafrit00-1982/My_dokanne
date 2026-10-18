package com.alafreet.dokan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.alafreet.dokan.data.dao.CategoryDao
import com.alafreet.dokan.data.dao.ProductDao
import com.alafreet.dokan.data.dao.SaleDao
import com.alafreet.dokan.data.entities.Category
import com.alafreet.dokan.data.entities.PaymentMethod
import com.alafreet.dokan.data.entities.Product
import com.alafreet.dokan.data.entities.Sale
import com.alafreet.dokan.data.entities.SaleItem

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
}

@Database(
    entities = [Product::class, Category::class, Sale::class, SaleItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dokan_alafreet_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
