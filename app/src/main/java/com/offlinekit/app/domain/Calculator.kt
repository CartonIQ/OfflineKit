package com.offlinekit.app.domain

import com.offlinekit.app.core.Outcome

/** Expression evaluator: + - * / parentheses, unary +/-, decimals. No UI dependencies. */
object Calculator {
    private const val MAX_LEN = 200

    fun evaluate(raw: String): Outcome<Double> {
        val s = raw.filterNot { it.isWhitespace() }
            .replace('×', '*').replace('÷', '/').replace('−', '-')
        if (s.isEmpty()) return Outcome.Err("Enter an expression")
        if (s.length > MAX_LEN) return Outcome.Err("Expression is too long")
        return try {
            val v = Parser(s).parse()
            if (v.isNaN() || v.isInfinite()) Outcome.Err("Result is too large")
            else Outcome.Ok(if (v == 0.0) 0.0 else v) // normalises -0.0
        } catch (e: CalcException) {
            Outcome.Err(e.message ?: "Invalid expression")
        }
    }

    private class CalcException(message: String) : Exception(message)

    private class Parser(private val s: String) {
        private var i = 0

        fun parse(): Double {
            val v = expr()
            if (i < s.length) throw CalcException("Unexpected '${s[i]}'")
            return v
        }

        private fun expr(): Double {
            var v = term()
            while (i < s.length && (s[i] == '+' || s[i] == '-')) {
                val op = s[i++]
                val r = term()
                v = if (op == '+') v + r else v - r
            }
            return v
        }

        private fun term(): Double {
            var v = factor()
            while (i < s.length && (s[i] == '*' || s[i] == '/')) {
                val op = s[i++]
                val r = factor()
                if (op == '*') v *= r
                else {
                    if (r == 0.0) throw CalcException("Cannot divide by zero")
                    v /= r
                }
            }
            return v
        }

        private fun factor(): Double {
            if (i >= s.length) throw CalcException("Incomplete expression")
            val c = s[i]
            return when {
                c == '+' -> { i++; factor() }
                c == '-' -> { i++; -factor() }
                c == '(' -> {
                    i++
                    val v = expr()
                    if (i >= s.length || s[i] != ')') throw CalcException("Missing closing parenthesis")
                    i++
                    v
                }
                c.isDigit() || c == '.' -> number()
                else -> throw CalcException("Unexpected '$c'")
            }
        }

        private fun number(): Double {
            val start = i
            var dots = 0
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                if (s[i] == '.') dots++
                i++
            }
            val t = s.substring(start, i)
            if (dots > 1 || t == ".") throw CalcException("Invalid number '$t'")
            return t.toDouble()
        }
    }
}
