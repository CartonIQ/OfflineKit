package com.offlinekit.app.domain

import com.offlinekit.app.core.Outcome

data class Measure(val name: String, val symbol: String, val toBase: Double)

enum class UnitCategory(val label: String, val units: List<Measure>) {
    LENGTH("Length", listOf(
        Measure("Millimeter", "mm", 0.001), Measure("Centimeter", "cm", 0.01),
        Measure("Meter", "m", 1.0), Measure("Kilometer", "km", 1000.0),
        Measure("Inch", "in", 0.0254), Measure("Foot", "ft", 0.3048),
        Measure("Yard", "yd", 0.9144), Measure("Mile", "mi", 1609.344),
    )),
    MASS("Mass", listOf(
        Measure("Milligram", "mg", 0.000001), Measure("Gram", "g", 0.001),
        Measure("Kilogram", "kg", 1.0), Measure("Tonne", "t", 1000.0),
        Measure("Ounce", "oz", 0.028349523125), Measure("Pound", "lb", 0.45359237),
    )),
    VOLUME("Volume", listOf(
        Measure("Milliliter", "mL", 0.001), Measure("Liter", "L", 1.0),
        Measure("Cubic meter", "m³", 1000.0), Measure("Teaspoon (US)", "tsp", 0.00492892159375),
        Measure("Tablespoon (US)", "tbsp", 0.01478676478125), Measure("Fluid ounce (US)", "fl oz", 0.0295735295625),
        Measure("Cup (US)", "cup", 0.2365882365), Measure("Gallon (US)", "gal", 3.785411784),
    )),
    TEMPERATURE("Temperature", listOf(
        Measure("Celsius", "°C", 1.0), Measure("Fahrenheit", "°F", 1.0), Measure("Kelvin", "K", 1.0),
    )),
}

object Converters {
    fun convert(cat: UnitCategory, value: Double, from: Measure, to: Measure): Outcome<Double> {
        if (cat == UnitCategory.TEMPERATURE) {
            val c = when (from.symbol) {
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            if (c < -273.15 - 1e-9) return Outcome.Err("Below absolute zero")
            val r = when (to.symbol) {
                "°F" -> c * 9.0 / 5.0 + 32.0
                "K" -> c + 273.15
                else -> c
            }
            return Outcome.Ok(r)
        }
        val r = value * from.toBase / to.toBase
        return if (r.isFinite()) Outcome.Ok(r) else Outcome.Err("Value is too large")
    }
}
