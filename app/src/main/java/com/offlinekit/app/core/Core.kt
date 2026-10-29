package com.offlinekit.app.core

import java.math.BigDecimal
import java.math.MathContext

/** Result of a calculation or validation: a value, or a human-readable error. */
sealed interface Outcome<out T> {
    data class Ok<out T>(val value: T) : Outcome<T>
    data class Err(val message: String) : Outcome<Nothing>
}

object Fmt {
    fun num(d: Double, digits: Int = 12): String =
        if (d.isNaN() || d.isInfinite()) "Error" else num(BigDecimal(d), digits)

    fun num(b: BigDecimal, digits: Int = 12): String {
        val r = b.round(MathContext(digits))
        if (r.signum() == 0) return "0"
        val s = r.stripTrailingZeros()
        return if (s.scale() < -15 || s.precision() - s.scale() > 15) s.toString() else s.toPlainString()
    }
}

object Parse {
    private val NUMBER = Regex("^[+-]?(\\d+\\.?\\d*|\\.\\d+)([eE][+-]?\\d+)?$")

    /** Strict decimal parsing (accepts a comma as decimal separator). Null if invalid or not finite. */
    fun double(text: String): Double? {
        val t = text.trim().replace(',', '.')
        if (t.isEmpty() || t.length > 60 || !NUMBER.matches(t)) return null
        return t.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    fun decimal(text: String): BigDecimal? {
        val t = text.trim().replace(',', '.')
        if (t.isEmpty() || t.length > 60 || !NUMBER.matches(t)) return null
        return t.toBigDecimalOrNull()
    }
}
