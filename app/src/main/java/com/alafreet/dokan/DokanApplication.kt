package com.alafreet.dokan

import android.app.Application
import com.alafreet.dokan.data.AppDatabase
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.notifications.NotificationHelper

class DokanApplication : Application() {

    lateinit var repository: DokanRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = DokanRepository(database)
        NotificationHelper.createNotificationChannel(this)
    }
}
