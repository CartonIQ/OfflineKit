package com.offlinekit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.offlinekit.app.ads.AdManager
import com.offlinekit.app.app.OfflineKitApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.start(this) // consent first; ads are optional and never block tools
        setContent { OfflineKitApp() }
    }
}
