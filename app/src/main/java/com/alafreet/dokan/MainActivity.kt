package com.alafreet.dokan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.alafreet.dokan.ui.navigation.DokanApp
import com.alafreet.dokan.ui.theme.DokanAlafreetTheme
import com.alafreet.dokan.utils.seedSampleDataIfEmpty
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as DokanApplication).repository

        lifecycleScope.launch { seedSampleDataIfEmpty(repository) }

        setContent {
            DokanAlafreetTheme {
                DokanApp(repository = repository)
            }
        }
    }
}
