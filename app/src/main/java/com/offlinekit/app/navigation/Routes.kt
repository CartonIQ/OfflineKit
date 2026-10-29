package com.offlinekit.app.navigation

data class ToolInfo(val id: String, val title: String, val glyph: String, val category: String, val blurb: String)

object Tools {
    val all = listOf(
        ToolInfo("calculator", "Calculator", "÷", "Calculate", "Arithmetic with parentheses"),
        ToolInfo("units", "Unit converter", "⇄", "Calculate", "Length, mass, volume, temperature"),
        ToolInfo("percent", "Percentage tools", "%", "Calculate", "Percent of, change, difference"),
        ToolInfo("datediff", "Date difference", "Δ", "Time", "Days between two dates"),
        ToolInfo("age", "Age calculator", "Y", "Time", "Years, months and days"),
        ToolInfo("timeconv", "Time converter", "⏱", "Time", "Seconds, minutes, hours, days"),
        ToolInfo("text", "Text tools", "Aa", "Text & security", "Counts and case conversion"),
        ToolInfo("qr", "QR generator", "▦", "Text & security", "Make a QR code on-device"),
        ToolInfo("password", "Password generator", "•••", "Text & security", "Secure random passwords"),
        ToolInfo("notes", "Notes", "✎", "Organize", "Private notes stored on this device"),
        ToolInfo("expenses", "Expense tracker", "¤", "Organize", "Entries, totals, date filter"),
        ToolInfo("storage", "Storage helper", "◔", "Organize", "App storage and free space"),
    )
    val categories: List<String> = all.map { it.category }.distinct()
    fun byId(id: String): ToolInfo? = all.firstOrNull { it.id == id }
}

enum class Tab { HOME, SAVED, SETTINGS }
