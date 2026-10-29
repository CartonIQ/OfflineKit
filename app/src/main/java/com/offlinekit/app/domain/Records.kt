package com.offlinekit.app.domain

import com.offlinekit.app.core.Outcome
import java.math.BigDecimal

data class Note(val id: String, val title: String, val body: String, val updatedAt: Long)

data class Expense(
    val id: String,
    val title: String,
    val category: String,
    val amountMinor: Long, // amount in hundredths, avoids floating-point money errors
    val epochDay: Long,
)

object NoteTools {
    fun search(notes: List<Note>, query: String): List<Note> {
        val q = query.trim()
        val hits = if (q.isEmpty()) notes
        else notes.filter { it.title.contains(q, ignoreCase = true) || it.body.contains(q, ignoreCase = true) }
        return hits.sortedByDescending { it.updatedAt }
    }
}

object ExpenseTools {
    val CATEGORIES = listOf("Food", "Transport", "Bills", "Shopping", "Entertainment", "Other")

    fun parseAmount(text: String): Outcome<Long> {
        val bd = com.offlinekit.app.core.Parse.decimal(text)?.stripTrailingZeros()
            ?: return Outcome.Err("Enter a valid amount")
        if (bd.signum() <= 0) return Outcome.Err("Amount must be greater than zero")
        if (bd.scale() > 2) return Outcome.Err("Use at most 2 decimal places")
        val minor = bd.movePointRight(2)
        if (minor > BigDecimal(Long.MAX_VALUE / 1000)) return Outcome.Err("Amount is too large")
        return Outcome.Ok(minor.longValueExact())
    }

    fun formatMinor(minor: Long): String = "${minor / 100}.${(minor % 100).toString().padStart(2, '0')}"

    fun totalsByCategory(items: List<Expense>): List<Pair<String, Long>> =
        items.groupBy { it.category }.map { (k, v) -> k to v.sumOf { it.amountMinor } }
            .sortedByDescending { it.second }

    fun filterByDate(items: List<Expense>, fromDay: Long?, toDay: Long?): List<Expense> =
        items.filter { (fromDay == null || it.epochDay >= fromDay) && (toDay == null || it.epochDay <= toDay) }
}
