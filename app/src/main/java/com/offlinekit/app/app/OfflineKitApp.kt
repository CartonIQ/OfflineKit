package com.offlinekit.app.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.offlinekit.app.data.Prefs
import com.offlinekit.app.navigation.AppNavigation
import com.offlinekit.app.ui.theme.OfflineKitTheme

@Composable
fun OfflineKitApp() {
    val ctx = LocalContext.current.applicationContext
    val prefs = remember { Prefs(ctx) }
    var theme by remember { mutableStateOf(prefs.theme) }
    var favorites by remember { mutableStateOf(prefs.favorites()) }
    var recents by remember { mutableStateOf(prefs.recents()) }

    OfflineKitTheme(theme) {
        AppNavigation(
            theme = theme,
            favorites = favorites,
            recents = recents,
            onThemeChange = { theme = it; prefs.theme = it },
            onToggleFavorite = { id ->
                prefs.setFavorite(id, id !in favorites)
                favorites = prefs.favorites()
            },
            onToolOpened = { id -> prefs.addRecent(id); recents = prefs.recents() },
        )
    }
}
