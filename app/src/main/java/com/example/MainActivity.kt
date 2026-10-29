package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.newswarajya.noswipe.reelshortblocker.service.monitor.AppMonitorService
import com.newswarajya.noswipe.reelshortblocker.ui.activities.home.NoScrollRootScreen
import com.newswarajya.noswipe.reelshortblocker.ui.theme.NoScrollTheme
import com.newswarajya.noswipe.reelshortblocker.ui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            startService(Intent(this, AppMonitorService::class.java))
        } catch (_: Exception) {
        }

        setContent {
            NoScrollTheme {
                NoScrollRootScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}
