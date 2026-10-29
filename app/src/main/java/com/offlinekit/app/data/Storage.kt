package com.offlinekit.app.data

import android.content.Context
import com.offlinekit.app.domain.Expense
import com.offlinekit.app.domain.Note
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

/** Small atomic file store (write temp file, then rename). Private app storage only. */
class JsonFileStore(private val file: File) {
    fun read(): String? = try { if (file.exists()) file.readText() else null } catch (e: IOException) { null }

    fun write(text: String): Boolean = try {
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(text)
        tmp.renameTo(file)
    } catch (e: IOException) { false }

    /** Keep an unreadable file instead of silently overwriting it. */
    fun backupCorrupt() { try { file.renameTo(File(file.parentFile, file.name + ".corrupt")) } catch (_: Exception) {} }
}

class NotesRepository(context: Context) {
    private val store = JsonFileStore(File(context.filesDir, "notes.json"))

    fun load(): List<Note> {
        val t = store.read() ?: return emptyList()
        return try {
            val a = JSONArray(t)
            (0 until a.length()).map {
                val o = a.getJSONObject(it)
                Note(o.getString("id"), o.optString("title"), o.optString("body"), o.optLong("updatedAt"))
            }
        } catch (e: JSONException) { store.backupCorrupt(); emptyList() }
    }

    fun save(list: List<Note>): Boolean {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("updatedAt", it.updatedAt)) }
        return store.write(a.toString())
    }
}

class ExpenseRepository(context: Context) {
    private val store = JsonFileStore(File(context.filesDir, "expenses.json"))

    fun load(): List<Expense> {
        val t = store.read() ?: return emptyList()
        return try {
            val a = JSONArray(t)
            (0 until a.length()).map {
                val o = a.getJSONObject(it)
                Expense(o.getString("id"), o.optString("title"), o.optString("category", "Other"), o.getLong("amountMinor"), o.getLong("epochDay"))
            }
        } catch (e: JSONException) { store.backupCorrupt(); emptyList() }
    }

    fun save(list: List<Expense>): Boolean {
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().put("id", it.id).put("title", it.title).put("category", it.category)
                .put("amountMinor", it.amountMinor).put("epochDay", it.epochDay))
        }
        return store.write(a.toString())
    }
}

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("offlinekit_prefs", Context.MODE_PRIVATE)

    var theme: String
        get() = sp.getString("theme", "system") ?: "system"
        set(v) { sp.edit().putString("theme", v).apply() }

    fun favorites(): Set<String> = sp.getStringSet("fav", emptySet())?.toSet() ?: emptySet()

    fun setFavorite(id: String, on: Boolean) {
        val s = favorites().toMutableSet()
        if (on) s.add(id) else s.remove(id)
        sp.edit().putStringSet("fav", s).apply()
    }

    fun recents(): List<String> = (sp.getString("recent", "") ?: "").split(",").filter { it.isNotBlank() }

    fun addRecent(id: String) {
        val l = (listOf(id) + recents().filter { it != id }).take(4)
        sp.edit().putString("recent", l.joinToString(",")).apply()
    }
}
