package com.offlinekit.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offlinekit.app.ads.AdBanner
import com.offlinekit.app.navigation.ToolInfo
import com.offlinekit.app.navigation.Tools

@Composable
fun HomeScreen(
    favorites: Set<String>,
    recents: List<String>,
    onOpen: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    Column(modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("OfflineKit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Everyday tools. Ready offline.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query, onValueChange = { if (it.length <= 60) query = it },
                placeholder = { Text("Search tools") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )
            val q = query.trim()
            if (q.isNotEmpty()) {
                val hits = Tools.all.filter { it.title.contains(q, true) || it.blurb.contains(q, true) }
                if (hits.isEmpty()) Text("No tools match “$q”.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                hits.forEach { ToolRow(it, it.id in favorites, { onOpen(it.id) }, { onToggleFavorite(it.id) }) }
            } else {
                val favTools = Tools.all.filter { it.id in favorites }
                if (favTools.isNotEmpty()) {
                    SectionTitle("Favorites")
                    favTools.forEach { ToolRow(it, true, { onOpen(it.id) }, { onToggleFavorite(it.id) }) }
                }
                val recentTools = recents.mapNotNull { Tools.byId(it) }
                if (recentTools.isNotEmpty()) {
                    SectionTitle("Recently used")
                    recentTools.forEach { ToolRow(it, it.id in favorites, { onOpen(it.id) }, { onToggleFavorite(it.id) }) }
                }
                Tools.categories.forEach { cat ->
                    SectionTitle(cat)
                    Tools.all.filter { it.category == cat }.forEach {
                        ToolRow(it, it.id in favorites, { onOpen(it.id) }, { onToggleFavorite(it.id) })
                    }
                }
            }
        }
        AdBanner()
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun ToolRow(tool: ToolInfo, favorite: Boolean, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    Surface(onClick = onOpen, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) { Text(tool.glyph, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tool.title, style = MaterialTheme.typography.titleMedium)
                Text(tool.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (favorite) "Remove ${tool.title} from favorites" else "Add ${tool.title} to favorites",
                    tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
