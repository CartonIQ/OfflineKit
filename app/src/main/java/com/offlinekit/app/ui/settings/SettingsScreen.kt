package com.offlinekit.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog


import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.offlinekit.app.ads.AdManager
import com.offlinekit.app.ui.home.SectionTitle

@Composable
fun SettingsScreen(theme: String, onThemeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val version = try {
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "?"
    } catch (e: Exception) { "?" }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SectionTitle("Theme")
        listOf("system" to "Follow system", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
            Row(
                Modifier.fillMaxWidth().clickable { onThemeChange(key) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = theme == key, onClick = { onThemeChange(key) })
                Text(label)
            }
        }

        SectionTitle("Privacy")
        Text(
            "All tools run on your device and need no account or internet connection. Notes and expenses " +
                "are stored in this app's private storage. Generated passwords are never saved or logged. " +
                "The app requests only the Internet permission, used by the Google AdMob ad SDK. " +
                "Ads are optional and never block a tool; AdMob's own data practices are described in Google's documentation.",
            style = MaterialTheme.typography.bodyMedium,
        )

        SectionTitle("Diagnostics")
        Text("Version $version", style = MaterialTheme.typography.bodyMedium)
        Text("Ad SDK ready: ${if (AdManager.ready) "yes" else "no (tools are unaffected)"}", style = MaterialTheme.typography.bodyMedium)

        SectionTitle("About")
        
        Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We respect your privacy.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { showPrivacyDialog = true }) {
            Text("Read Full Privacy Policy", color = MaterialTheme.colorScheme.primary)
        }

    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text("Last updated: October 2026\n\nOfflineKit is a 100% offline tool. We do not collect, store, or share any personal information. All data (notes, expenses) is stored locally on your device.\n\nAds: We use Google AdMob to show ads. AdMob may collect your device's advertising ID.\n\nBy using OfflineKit, you agree to this policy. For questions, contact cartoniq.business@gmail.com.", style = MaterialTheme.typography.bodySmall)
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
