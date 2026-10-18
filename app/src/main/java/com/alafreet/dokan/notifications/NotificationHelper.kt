package com.alafreet.dokan.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "dokan_low_stock_channel"
    private const val CHANNEL_NAME = "تنبيهات المخزون - دكان العفريت"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تنبيهات عند اقتراب أو نفاد كمية أحد المنتجات في دكان العفريت"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun notifyLowStock(context: Context, productName: String, currentQuantity: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("تنبيه نقص مخزون - دكان العفريت")
            .setContentText("المنتج \"$productName\" أوشك على النفاد (المتبقي: $currentQuantity)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context)
                .notify(productName.hashCode(), notification)
        }
    }
}
