package com.offlinekit.app.domain

import com.offlinekit.app.core.Outcome
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import java.util.Random
import java.security.SecureRandom
import kotlin.math.abs

object PercentTools {
    fun percentOf(percent: Double, value: Double): Double = percent / 100.0 * value

    /** Percentage change from [from] to [to]; undefined when [from] is zero. */
    fun change(from: Double, to: Double): Outcome<Double> =
        if (from == 0.0) Outcome.Err("Starting value cannot be zero")
        else Outcome.Ok((to - from) / abs(from) * 100.0)

    /** Symmetric percentage difference |a-b| / mean(a,b). Non-negative inputs only. */
    fun difference(a: Double, b: Double): Outcome<Double> = when {
        a < 0 || b < 0 -> Outcome.Err("Values must not be negative")
        a + b == 0.0 -> Outcome.Err("Both values are zero")
        else -> Outcome.Ok(abs(a - b) / ((a + b) / 2.0) * 100.0)
    }
}

object DateTools {
    /** Whole days between two dates, order-independent. Inclusive counts both end dates. */
    fun daysBetween(a: LocalDate, b: LocalDate, inclusive: Boolean): Long {
        val d = abs(ChronoUnit.DAYS.between(a, b))
        return if (inclusive) d + 1 else d
    }

    fun breakdown(a: LocalDate, b: LocalDate): Period =
        if (a.isAfter(b)) Period.between(b, a) else Period.between(a, b)

    fun age(birth: LocalDate, today: LocalDate): Outcome<Period> =
        if (birth.isAfter(today)) Outcome.Err("Birth date is in the future")
        else Outcome.Ok(Period.between(birth, today))
}

enum class TimeUnitKind(val label: String, val seconds: BigDecimal) {
    SECONDS("Seconds", BigDecimal(1)),
    MINUTES("Minutes", BigDecimal(60)),
    HOURS("Hours", BigDecimal(3600)),
    DAYS("Days", BigDecimal(86400)),
}

object TimeConverter {
    fun convert(value: BigDecimal, from: TimeUnitKind, to: TimeUnitKind): Outcome<BigDecimal> = try {
        Outcome.Ok(value.multiply(from.seconds).divide(to.seconds, MathContext.DECIMAL64))
    } catch (e: ArithmeticException) {
        Outcome.Err("Value is too large")
    }
}

data class TextStats(val characters: Int, val words: Int, val lines: Int)

object TextTools {
    fun stats(t: String): TextStats = TextStats(
        characters = t.codePointCount(0, t.length),
        words = t.split(Regex("\\s+")).count { it.isNotEmpty() },
        lines = if (t.isEmpty()) 0 else t.lines().size,
    )

    fun titleCase(t: String): String {
        val sb = StringBuilder(t.length)
        var startOfWord = true
        for (ch in t) {
            sb.append(if (startOfWord && ch.isLetter()) ch.uppercaseChar() else ch.lowercaseChar())
            startOfWord = ch.isWhitespace()
        }
        return sb.toString()
    }
}

data class PasswordOptions(
    val length: Int,
    val lower: Boolean = true,
    val upper: Boolean = true,
    val digits: Boolean = true,
    val symbols: Boolean = true,
)

object PasswordGenerator {
    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 64
    const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val DIGITS = "0123456789"
    const val SYMBOLS = "!@#$%^&*()-_=+[]{};:,.?"

    /** Uses SecureRandom by default; every selected character set appears at least once. Never logs. */
    fun generate(o: PasswordOptions, random: Random = SecureRandom()): Outcome<String> {
        val sets = buildList {
            if (o.lower) add(LOWER)
            if (o.upper) add(UPPER)
            if (o.digits) add(DIGITS)
            if (o.symbols) add(SYMBOLS)
        }
        if (sets.isEmpty()) return Outcome.Err("Select at least one character type")
        if (o.length !in MIN_LENGTH..MAX_LENGTH) return Outcome.Err("Length must be $MIN_LENGTH–$MAX_LENGTH")
        val all = sets.joinToString("")
        val chars = MutableList(o.length) { all[random.nextInt(all.length)] }
        val positions = (0 until o.length).shuffled(random).take(sets.size)
        sets.forEachIndexed { idx, set -> chars[positions[idx]] = set[random.nextInt(set.length)] }
        return Outcome.Ok(chars.joinToString(""))
    }
}
