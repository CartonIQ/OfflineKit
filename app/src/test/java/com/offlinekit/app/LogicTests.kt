package com.offlinekit.app

import com.offlinekit.app.core.Fmt
import com.offlinekit.app.core.Outcome
import com.offlinekit.app.core.Parse
import com.offlinekit.app.domain.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Random

private fun <T> Outcome<T>.ok(): T = (this as Outcome.Ok<T>).value
private fun Outcome<*>.isErr() = this is Outcome.Err

class CalculatorTest {
    private fun eval(s: String) = Calculator.evaluate(s)
    @Test fun precedence() = assertEquals(14.0, eval("2+3*4").ok(), 0.0)
    @Test fun parentheses() = assertEquals(20.0, eval("(2+3)*4").ok(), 0.0)
    @Test fun decimals() = assertEquals(2.5, eval("10÷4").ok(), 0.0)
    @Test fun unaryMinus() { assertEquals(-3.0, eval("-5+2").ok(), 0.0); assertEquals(-6.0, eval("2*-3").ok(), 0.0) }
    @Test fun keypadSymbols() = assertEquals(7.0, eval("3×4−5").ok(), 0.0)
    @Test fun divideByZero() = assertTrue(eval("1/0").isErr())
    @Test fun malformed() { listOf("2+", "(1+2", "", "1.2.3", "abc", "2)(3", ")").forEach { assertTrue(it, eval(it).isErr()) } }
    @Test fun floatNoise() = assertEquals("0.3", Fmt.num(eval("0.1+0.2").ok()))
    @Test fun tooLong() = assertTrue(eval("1+".repeat(150) + "1").isErr())
    @Test fun negativeZeroIsZero() = assertEquals("0", Fmt.num(eval("0*-1").ok()))
}

class FormatParseTest {
    @Test fun formatting() {
        assertEquals("1000", Fmt.num(1000.0)); assertEquals("-2.5", Fmt.num(-2.5)); assertEquals("0", Fmt.num(0.0))
        assertEquals("Error", Fmt.num(Double.NaN))
    }
    @Test fun strictParsing() {
        assertEquals(1.5, Parse.double("1,5")!!, 0.0)
        assertEquals(-2.0, Parse.double(" -2 ")!!, 0.0)
        listOf("", "abc", "1d", "NaN", "Infinity", "0x10", "1..2").forEach { assertNull(it, Parse.double(it)) }
    }
}

class ConvertersTest {
    private fun m(c: UnitCategory, s: String) = c.units.first { it.symbol == s }
    @Test fun length() = assertEquals(1000.0, Converters.convert(UnitCategory.LENGTH, 1.0, m(UnitCategory.LENGTH, "km"), m(UnitCategory.LENGTH, "m")).ok(), 1e-9)
    @Test fun mile() = assertEquals(1.609344, Converters.convert(UnitCategory.LENGTH, 1.0, m(UnitCategory.LENGTH, "mi"), m(UnitCategory.LENGTH, "km")).ok(), 1e-9)
    @Test fun mass() = assertEquals(2.2046226, Converters.convert(UnitCategory.MASS, 1.0, m(UnitCategory.MASS, "kg"), m(UnitCategory.MASS, "lb")).ok(), 1e-6)
    @Test fun volume() = assertEquals(1000.0, Converters.convert(UnitCategory.VOLUME, 1.0, m(UnitCategory.VOLUME, "L"), m(UnitCategory.VOLUME, "mL")).ok(), 1e-9)
    @Test fun boiling() = assertEquals(212.0, Converters.convert(UnitCategory.TEMPERATURE, 100.0, m(UnitCategory.TEMPERATURE, "°C"), m(UnitCategory.TEMPERATURE, "°F")).ok(), 1e-9)
    @Test fun kelvin() = assertEquals(-273.15, Converters.convert(UnitCategory.TEMPERATURE, 0.0, m(UnitCategory.TEMPERATURE, "K"), m(UnitCategory.TEMPERATURE, "°C")).ok(), 1e-9)
    @Test fun belowAbsoluteZero() = assertTrue(Converters.convert(UnitCategory.TEMPERATURE, -300.0, m(UnitCategory.TEMPERATURE, "°C"), m(UnitCategory.TEMPERATURE, "K")).isErr())
    @Test fun negativeLength() = assertEquals(-1000.0, Converters.convert(UnitCategory.LENGTH, -1.0, m(UnitCategory.LENGTH, "km"), m(UnitCategory.LENGTH, "m")).ok(), 1e-9)
}

class PercentTest {
    @Test fun percentOf() = assertEquals(30.0, PercentTools.percentOf(20.0, 150.0), 1e-9)
    @Test fun increase() = assertEquals(50.0, PercentTools.change(50.0, 75.0).ok(), 1e-9)
    @Test fun decrease() = assertEquals(-25.0, PercentTools.change(80.0, 60.0).ok(), 1e-9)
    @Test fun changeFromZero() = assertTrue(PercentTools.change(0.0, 5.0).isErr())
    @Test fun difference() = assertEquals(200.0 / 3.0, PercentTools.difference(100.0, 50.0).ok(), 1e-9)
    @Test fun differenceBothZero() = assertTrue(PercentTools.difference(0.0, 0.0).isErr())
    @Test fun differenceNegative() = assertTrue(PercentTools.difference(-1.0, 5.0).isErr())
}

class DateTest {
    @Test fun leapYearSpan() {
        val a = LocalDate.of(2024, 2, 28); val b = LocalDate.of(2024, 3, 1)
        assertEquals(2L, DateTools.daysBetween(a, b, false))
        assertEquals(3L, DateTools.daysBetween(a, b, true))
        assertEquals(2L, DateTools.daysBetween(b, a, false))
    }
    @Test fun sameDay() { val d = LocalDate.of(2025, 1, 1); assertEquals(0L, DateTools.daysBetween(d, d, false)); assertEquals(1L, DateTools.daysBetween(d, d, true)) }
    @Test fun nonLeapFeb() = assertEquals(1L, DateTools.daysBetween(LocalDate.of(2023, 2, 28), LocalDate.of(2023, 3, 1), false))
    @Test fun age() {
        val p = DateTools.age(LocalDate.of(2000, 1, 15), LocalDate.of(2024, 3, 20)).ok()
        assertEquals(Triple(24, 2, 5), Triple(p.years, p.months, p.days))
    }
    @Test fun ageBirthdayToday() { val d = LocalDate.of(2020, 5, 5); val p = DateTools.age(d, d).ok(); assertEquals(Triple(0, 0, 0), Triple(p.years, p.months, p.days)) }
    @Test fun futureBirth() = assertTrue(DateTools.age(LocalDate.of(2030, 1, 1), LocalDate.of(2026, 1, 1)).isErr())
}

class TimeConverterTest {
    private fun conv(v: String, f: TimeUnitKind, t: TimeUnitKind) = TimeConverter.convert(BigDecimal(v), f, t).ok()
    @Test fun hoursToSeconds() = assertEquals(0, BigDecimal("7200").compareTo(conv("2", TimeUnitKind.HOURS, TimeUnitKind.SECONDS)))
    @Test fun minutesToHours() = assertEquals(0, BigDecimal("1.5").compareTo(conv("90", TimeUnitKind.MINUTES, TimeUnitKind.HOURS)))
    @Test fun daysToHours() = assertEquals(0, BigDecimal("24").compareTo(conv("1", TimeUnitKind.DAYS, TimeUnitKind.HOURS)))
    @Test fun hugeValueDoesNotCrash() {
        val r = TimeConverter.convert(BigDecimal("1E+400"), TimeUnitKind.DAYS, TimeUnitKind.SECONDS)
        assertTrue(r is Outcome.Ok)
    }
}

class TextToolsTest {
    @Test fun empty() = assertEquals(TextStats(0, 0, 0), TextTools.stats(""))
    @Test fun counts() = assertEquals(TextStats(23, 4, 2), TextTools.stats("Hello world\nsecond line"))
    @Test fun whitespaceOnly() { val s = TextTools.stats("   "); assertEquals(0, s.words); assertEquals(3, s.characters) }
    @Test fun emojiCountsAsOneCharacter() = assertEquals(2, TextTools.stats("a😀").characters)
    @Test fun title() = assertEquals("Hello World", TextTools.titleCase("hello wORLD"))
    @Test fun crlf() = assertEquals(2, TextTools.stats("a\r\nb").lines)
}

class PasswordTest {
    private val all = PasswordOptions(16)
    @Test fun lengthAndClasses() {
        repeat(200) {
            val p = PasswordGenerator.generate(all).ok()
            assertEquals(16, p.length)
            assertTrue(p.any { it in PasswordGenerator.LOWER }); assertTrue(p.any { it in PasswordGenerator.UPPER })
            assertTrue(p.any { it in PasswordGenerator.DIGITS }); assertTrue(p.any { it in PasswordGenerator.SYMBOLS })
        }
    }
    @Test fun digitsOnly() = assertTrue(PasswordGenerator.generate(PasswordOptions(10, false, false, true, false)).ok().all { it.isDigit() })
    @Test fun noSets() = assertTrue(PasswordGenerator.generate(PasswordOptions(16, false, false, false, false)).isErr())
    @Test fun badLength() { assertTrue(PasswordGenerator.generate(PasswordOptions(3)).isErr()); assertTrue(PasswordGenerator.generate(PasswordOptions(65)).isErr()) }
    @Test fun deterministicWithSeed() = assertEquals(PasswordGenerator.generate(all, Random(1)).ok(), PasswordGenerator.generate(all, Random(1)).ok())
}

class RecordsTest {
    @Test fun parseAmount() {
        assertEquals(1250L, ExpenseTools.parseAmount("12.5").ok()); assertEquals(7L, ExpenseTools.parseAmount("0.07").ok())
        assertEquals(1250L, ExpenseTools.parseAmount("12.500").ok()); assertEquals(1500L, ExpenseTools.parseAmount("15").ok())
        listOf("0", "-3", "1.234", "abc", "", "1e30").forEach { assertTrue(it, ExpenseTools.parseAmount(it).isErr()) }
    }
    @Test fun formatMinor() { assertEquals("12.50", ExpenseTools.formatMinor(1250)); assertEquals("0.07", ExpenseTools.formatMinor(7)); assertEquals("0.00", ExpenseTools.formatMinor(0)) }
    private val items = listOf(
        Expense("1", "a", "Food", 1000, 10), Expense("2", "b", "Food", 250, 20), Expense("3", "c", "Bills", 5000, 30),
    )
    @Test fun totals() = assertEquals(listOf("Bills" to 5000L, "Food" to 1250L), ExpenseTools.totalsByCategory(items))
    @Test fun dateFilter() {
        assertEquals(listOf("2"), ExpenseTools.filterByDate(items, 15, 25).map { it.id })
        assertEquals(3, ExpenseTools.filterByDate(items, null, null).size)
        assertEquals(listOf("3"), ExpenseTools.filterByDate(items, 30, null).map { it.id })
    }
    @Test fun noteSearch() {
        val notes = listOf(Note("1", "Shopping", "milk", 1), Note("2", "Idea", "Say HELLO", 5), Note("3", "hello list", "", 3))
        assertEquals(listOf("2", "3"), NoteTools.search(notes, "hello").map { it.id })
        assertEquals(listOf("2", "3", "1"), NoteTools.search(notes, "  ").map { it.id })
        assertTrue(NoteTools.search(notes, "zzz").isEmpty())
    }
}
