package com.offlinekit.app.ui.tools

import android.os.StatFs
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import com.offlinekit.app.core.Outcome
import com.offlinekit.app.data.ExpenseRepository
import com.offlinekit.app.data.NotesRepository
import com.offlinekit.app.domain.Expense
import com.offlinekit.app.domain.ExpenseTools
import com.offlinekit.app.domain.Note
import com.offlinekit.app.domain.NoteTools
import com.offlinekit.app.ui.components.DateField
import com.offlinekit.app.ui.components.ErrorText
import com.offlinekit.app.ui.components.HintText
import com.offlinekit.app.ui.components.Picker
import com.offlinekit.app.ui.components.ToggleRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date
import java.util.UUID

@Composable
fun ColumnScope.NotesTool() {
    val ctx = LocalContext.current
    val repo = remember { NotesRepository(ctx.applicationContext) }
    val notes = remember { mutableStateListOf<Note>().also { it.addAll(repo.load()) } }
    var query by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var toDelete by remember { mutableStateOf<Note?>(null) }
    var saveFailed by remember { mutableStateOf(false) }

    fun upsert(n: Note) {
        val i = notes.indexOfFirst { it.id == n.id }
        if (i >= 0) notes[i] = n else notes.add(0, n)
        saveFailed = !repo.save(notes.toList())
    }

    val id = editingId
    if (id != null) {
        val existing = notes.firstOrNull { it.id == id }
        var title by rememberSaveable(id) { mutableStateOf(existing?.title ?: "") }
        var body by rememberSaveable(id) { mutableStateOf(existing?.body ?: "") }
        fun commit() {
            if (title.isBlank() && body.isBlank()) return // never store empty notes
            val current = notes.firstOrNull { it.id == id }
            if (current == null || current.title != title || current.body != body) {
                upsert(Note(id, title, body, System.currentTimeMillis()))
            }
        }
        BackHandler { commit(); editingId = null }
        // Debounced autosave while typing.
        LaunchedEffect(title, body) { delay(600); commit() }

        OutlinedTextField(title, { if (it.length <= 200) title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(body, { if (it.length <= 20_000) body = it }, label = { Text("Note") }, minLines = 10, modifier = Modifier.fillMaxWidth())
        if (saveFailed) ErrorText("Could not save. Check free storage space.")
        Button({ commit(); editingId = null }, Modifier.fillMaxWidth()) { Text("Done") }
        return
    }

    Button({ editingId = UUID.randomUUID().toString() }, Modifier.fillMaxWidth()) { Text("New note") }
    OutlinedTextField(
        query, { if (it.length <= 60) query = it }, placeholder = { Text("Search notes") },
        leadingIcon = { Icon(Icons.Filled.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
    )
    if (saveFailed) ErrorText("Could not save. Check free storage space.")
    val shown = NoteTools.search(notes.toList(), query)
    if (shown.isEmpty()) {
        HintText(if (notes.isEmpty()) "No notes yet. Tap “New note” to write one." else "No notes match your search.")
    }
    val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    shown.forEach { n ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable { editingId = n.id }) {
            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(n.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (n.body.isNotBlank()) Text(n.body, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(df.format(Date(n.updatedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton({ toDelete = n }) { Icon(Icons.Filled.Delete, contentDescription = "Delete note") }
            }
        }
    }
    toDelete?.let { n ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete note?") },
            text = { Text("“${n.title.ifBlank { "Untitled" }}” will be permanently deleted.") },
            confirmButton = { TextButton({ notes.removeAll { it.id == n.id }; saveFailed = !repo.save(notes.toList()); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
fun ColumnScope.ExpensesTool() {
    val ctx = LocalContext.current
    val repo = remember { ExpenseRepository(ctx.applicationContext) }
    val items = remember { mutableStateListOf<Expense>().also { it.addAll(repo.load()) } }
    val today = LocalDate.now().toEpochDay()

    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(ExpenseTools.CATEGORIES[0]) }
    var day by rememberSaveable { mutableStateOf(today) }
    var error by remember { mutableStateOf<String?>(null) }
    var filterOn by rememberSaveable { mutableStateOf(false) }
    var from by rememberSaveable { mutableStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay()) }
    var to by rememberSaveable { mutableStateOf(today) }
    var toDelete by remember { mutableStateOf<Expense?>(null) }

    fun resetForm() { editingId = null; title = ""; amount = ""; day = today; error = null }
    fun persist() { if (!repo.save(items.toList())) error = "Could not save. Check free storage space." }

    Text(if (editingId == null) "Add expense" else "Edit expense", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(title, { if (it.length <= 80) title = it }, label = { Text("Description") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(
        amount, { if (it.length <= 15) amount = it }, label = { Text("Amount") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
    )
    Picker("Category", ExpenseTools.CATEGORIES, category, { it }, { category = it })
    DateField("Date", day, { day = it })
    error?.let { ErrorText(it) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                when (val a = ExpenseTools.parseAmount(amount)) {
                    is Outcome.Err -> error = a.message
                    is Outcome.Ok -> {
                        val e = Expense(editingId ?: UUID.randomUUID().toString(), title.trim().ifEmpty { "Expense" }, category, a.value, day)
                        val i = items.indexOfFirst { it.id == e.id }
                        if (i >= 0) items[i] = e else items.add(0, e)
                        resetForm()
                        persist()
                    }
                }
            },
            modifier = Modifier.weight(1f),
        ) { Text(if (editingId == null) "Add" else "Save changes") }
        if (editingId != null) OutlinedButton({ resetForm() }, Modifier.weight(1f)) { Text("Cancel") }
    }

    ToggleRow("Filter by date", filterOn) { filterOn = it }
    if (filterOn) {
        DateField("From", from, { from = it })
        DateField("To", to, { to = it })
    }
    val shown = (if (filterOn) ExpenseTools.filterByDate(items.toList(), from, to) else items.toList())
        .sortedByDescending { it.epochDay }

    Text("Totals", style = MaterialTheme.typography.titleMedium)
    if (shown.isEmpty()) HintText("No expenses to show.")
    else {
        ExpenseTools.totalsByCategory(shown).forEach { (cat, sum) ->
            Row(Modifier.fillMaxWidth()) { Text(cat, Modifier.weight(1f)); Text(ExpenseTools.formatMinor(sum)) }
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Total", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Text(ExpenseTools.formatMinor(shown.sumOf { it.amountMinor }), style = MaterialTheme.typography.titleSmall)
        }
        HintText("Amounts are in your own currency. This is a simple log, not financial advice.")
    }

    shown.forEach { e ->
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable {
            editingId = e.id; title = e.title; amount = ExpenseTools.formatMinor(e.amountMinor); category = e.category; day = e.epochDay; error = null
        }) {
            Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(e.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${e.category} · ${LocalDate.ofEpochDay(e.epochDay)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(ExpenseTools.formatMinor(e.amountMinor), Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
                IconButton({ toDelete = e }) { Icon(Icons.Filled.Delete, contentDescription = "Delete expense") }
            }
        }
    }
    toDelete?.let { e ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete expense?") },
            text = { Text("“${e.title}” (${ExpenseTools.formatMinor(e.amountMinor)}) will be permanently deleted.") },
            confirmButton = { TextButton({ items.removeAll { it.id == e.id }; if (editingId == e.id) resetForm(); persist(); toDelete = null }) { Text("Delete") } },
            dismissButton = { TextButton({ toDelete = null }) { Text("Cancel") } },
        )
    }
}

private data class StorageInfo(val files: Long, val cache: Long, val free: Long, val total: Long)

private fun dirSize(f: File): Long =
    if (f.isFile) f.length() else f.listFiles()?.sumOf { dirSize(it) } ?: 0L

@Composable
fun ColumnScope.StorageTool() {
    val ctx = LocalContext.current
    var info by remember { mutableStateOf<StorageInfo?>(null) }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(refresh) {
        info = withContext(Dispatchers.IO) {
            val st = StatFs(ctx.filesDir.path)
            StorageInfo(dirSize(ctx.filesDir), dirSize(ctx.cacheDir), st.availableBytes, st.totalBytes)
        }
    }
    val i = info
    if (i == null) HintText("Measuring…")
    else {
        fun fmt(b: Long) = Formatter.formatFileSize(ctx, b)
        listOf(
            "Saved data (notes, expenses)" to fmt(i.files),
            "Cache" to fmt(i.cache),
            "Device free space" to fmt(i.free),
            "Device total space" to fmt(i.total),
        ).forEach { (k, v) ->
            Row(Modifier.fillMaxWidth()) { Text(k, Modifier.weight(1f)); Text(v, style = MaterialTheme.typography.titleSmall) }
        }
    }
    OutlinedButton(
        onClick = { ctx.cacheDir.listFiles()?.forEach { it.deleteRecursively() }; refresh++ },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Clear cache") }
    HintText("Android does not let apps inspect other apps' files. This screen shows only OfflineKit's own storage plus overall free space on the device. Clearing the cache never touches your notes or expenses.")
}
