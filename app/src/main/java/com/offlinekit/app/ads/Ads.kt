package com.offlinekit.app.ads

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdConfig {
    /** GOOGLE TEST banner unit. Replace with your real unit ID in release builds only. */
    const val BANNER_UNIT_ID = "ca-app-pub-9771662959477951/4256091073"
}

/** Everything ad-related lives here; tool code never depends on it. Any failure just means "no ad". */
object AdManager {
    var ready by mutableStateOf(false)
        private set
    private var started = false
    private var initializing = false

    fun start(activity: Activity) {
        if (started) return
        started = true
        try {
            val info = UserMessagingPlatform.getConsentInformation(activity)
            info.requestConsentInfoUpdate(
                activity,
                ConsentRequestParameters.Builder().build(),
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ -> initSdk(activity, info) }
                },
                { _ -> initSdk(activity, info) },
            )
            initSdk(activity, info) // already consented on a previous launch
        } catch (_: Throwable) {
            // Ads unavailable: ignore.
        }
    }

    private fun initSdk(activity: Activity, info: ConsentInformation) {
        if (ready || initializing || !info.canRequestAds()) return
        initializing = true
        try {
            MobileAds.initialize(activity.applicationContext) { ready = true }
        } catch (_: Throwable) {
            initializing = false
        }
    }
}

/** Reserved 50dp strip so layout never jumps when an ad loads or fails. Home screen only. */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
        if (AdManager.ready) {
            AndroidView(
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdConfig.BANNER_UNIT_ID
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
