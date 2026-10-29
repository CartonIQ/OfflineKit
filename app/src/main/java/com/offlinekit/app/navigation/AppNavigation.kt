package com.offlinekit.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.offlinekit.app.ui.components.ToolScaffold
import com.offlinekit.app.ui.home.HomeScreen
import com.offlinekit.app.ui.saved.SavedScreen
import com.offlinekit.app.ui.settings.SettingsScreen
import com.offlinekit.app.ui.tools.*

@Composable
fun AppNavigation(
    theme: String,
    favorites: Set<String>,
    recents: List<String>,
    onThemeChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToolOpened: (String) -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var openTool by rememberSaveable { mutableStateOf<String?>(null) }

    val tool = openTool?.let { Tools.byId(it) }
    if (tool != null) {
        BackHandler { openTool = null }
        ToolScaffold(title = tool.title, onBack = { openTool = null }) {
            when (tool.id) {
                "calculator" -> CalculatorTool()
                "units" -> UnitConverterTool()
                "percent" -> PercentTool()
                "datediff" -> DateDiffTool()
                "age" -> AgeTool()
                "timeconv" -> TimeConverterTool()
                "text" -> TextTool()
                "qr" -> QrTool()
                "password" -> PasswordTool()
                "notes" -> NotesTool()
                "expenses" -> ExpensesTool()
                "storage" -> StorageTool()
            }
        }
        return
    }

    val open: (String) -> Unit = { id -> onToolOpened(id); openTool = id }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == Tab.HOME, { tab = Tab.HOME }, { Icon(Icons.Filled.Home, null) }, label = { Text("Home") })
                NavigationBarItem(tab == Tab.SAVED, { tab = Tab.SAVED }, { Icon(Icons.Filled.Favorite, null) }, label = { Text("Saved") })
                NavigationBarItem(tab == Tab.SETTINGS, { tab = Tab.SETTINGS }, { Icon(Icons.Filled.Settings, null) }, label = { Text("Settings") })
            }
        },
    ) { inner ->
        val m = Modifier.padding(inner)
        when (tab) {
            Tab.HOME -> HomeScreen(favorites, recents, open, onToggleFavorite, m)
            Tab.SAVED -> SavedScreen(favorites, open, onToggleFavorite, m)
            Tab.SETTINGS -> SettingsScreen(theme, onThemeChange, m)
        }
    }
}
