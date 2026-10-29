package com.offlinekit.app.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offlinekit.app.navigation.Tools
import com.offlinekit.app.ui.home.SectionTitle
import com.offlinekit.app.ui.home.ToolRow

@Composable
fun SavedScreen(
    favorites: Set<String>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Saved", style = MaterialTheme.typography.headlineMedium)
        SectionTitle("Favorites")
        val favs = Tools.all.filter { it.id in favorites }
        if (favs.isEmpty()) {
            Text("No favorites yet. Tap the heart on any tool to pin it here.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        favs.forEach { ToolRow(it, true, { onOpen(it.id) }, { onToggleFavorite(it.id) }) }
        SectionTitle("Your saved data")
        listOf("notes", "expenses").mapNotNull { Tools.byId(it) }.forEach {
            ToolRow(it, it.id in favorites, { onOpen(it.id) }, { onToggleFavorite(it.id) })
        }
    }
}
