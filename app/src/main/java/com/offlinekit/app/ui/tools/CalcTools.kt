package com.offlinekit.app.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import com.offlinekit.app.core.Fmt
import com.offlinekit.app.core.Outcome
import com.offlinekit.app.core.Parse
import com.offlinekit.app.domain.Calculator
import com.offlinekit.app.domain.Converters
import com.offlinekit.app.domain.PercentTools
import com.offlinekit.app.domain.UnitCategory
import com.offlinekit.app.ui.components.ErrorText
import com.offlinekit.app.ui.components.HintText
import com.offlinekit.app.ui.components.Picker
import com.offlinekit.app.ui.components.ResultCard

private val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

@Composable
fun ColumnScope.CalculatorTool() {
    var expr by rememberSaveable { mutableStateOf("") }
    val live = if (expr.isBlank()) null else Calculator.evaluate(expr)

    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(if (expr.isEmpty()) "0" else expr, fontSize = 30.sp, maxLines = 3)
            when (live) {
                is Outcome.Ok -> Text("= ${Fmt.num(live.value)}", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                is Outcome.Err -> HintText(live.message)
                null -> HintText("Use the keypad")
            }
        }
    }
    val rows = listOf(
        listOf("C", "(", ")", "÷"), listOf("7", "8", "9", "×"), listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"), listOf("0", ".", "⌫", "="),
    )
    val operators = setOf("÷", "×", "−", "+", "=")
    rows.forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { k ->
                val click: () -> Unit = {
                    when (k) {
                        "C" -> expr = ""
                        "⌫" -> expr = expr.dropLast(1)
                        "=" -> {
                            val r = Calculator.evaluate(expr)
                            if (r is Outcome.Ok) {
                                val s = Fmt.num(r.value)
                                if ('E' !in s) expr = s // keep expression if result needs scientific notation
                            }
                        }
                        else -> if (expr.length < 200) expr += k
                    }
                }
                val m = Modifier.weight(1f).height(64.dp)
                if (k in operators) Button(onClick = click, modifier = m) { Text(k, fontSize = 22.sp) }
                else FilledTonalButton(onClick = click, modifier = m) { Text(k, fontSize = 22.sp) }
            }
        }
    }
}

@Composable
fun ColumnScope.UnitConverterTool() {
    val cats = UnitCategory.entries
    var catIdx by rememberSaveable { mutableStateOf(0) }
    var fromIdx by rememberSaveable { mutableStateOf(0) }
    var toIdx by rememberSaveable { mutableStateOf(1) }
    var input by rememberSaveable { mutableStateOf("") }
    val cat = cats[catIdx]
    val from = cat.units[fromIdx.coerceIn(cat.units.indices)]
    val to = cat.units[toIdx.coerceIn(cat.units.indices)]

    Picker("Category", cats, cat, { it.label }, { catIdx = cats.indexOf(it); fromIdx = 0; toIdx = 1 })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = input, onValueChange = { if (it.length <= 40) input = it },
            label = { Text("Value") }, singleLine = true, keyboardOptions = decimal, modifier = Modifier.weight(1f),
        )
        OutlinedButton(
            onClick = { input = if (input.startsWith("-")) input.drop(1) else "-$input" },
            modifier = Modifier.height(56.dp).padding(top = 8.dp),
        ) { Text("±") }
    }
    Picker("From", cat.units, from, { "${it.name} (${it.symbol})" }, { fromIdx = cat.units.indexOf(it) })
    Picker("To", cat.units, to, { "${it.name} (${it.symbol})" }, { toIdx = cat.units.indexOf(it) })
    OutlinedButton(onClick = { val t = fromIdx; fromIdx = toIdx; toIdx = t }, modifier = Modifier.fillMaxWidth()) { Text("Swap units") }

    if (input.isBlank()) HintText("Enter a value to convert.")
    else {
        val v = Parse.double(input)
        if (v == null) ErrorText("Enter a valid number.")
        else when (val r = Converters.convert(cat, v, from, to)) {
            is Outcome.Ok -> ResultCard("${Fmt.num(v)} ${from.symbol} =", "${Fmt.num(r.value, 10)} ${to.symbol}")
            is Outcome.Err -> ErrorText(r.message)
        }
    }
}

private enum class PercentMode(val label: String, val aLabel: String, val bLabel: String) {
    OF("X% of a value", "Percent (%)", "Value"),
    CHANGE("Increase / decrease", "From", "To"),
    DIFF("Percentage difference", "First value", "Second value"),
}

@Composable
fun ColumnScope.PercentTool() {
    val modes = PercentMode.entries
    var modeIdx by rememberSaveable { mutableStateOf(0) }
    var a by rememberSaveable { mutableStateOf("") }
    var b by rememberSaveable { mutableStateOf("") }
    val mode = modes[modeIdx]

    Picker("Mode", modes, mode, { it.label }, { modeIdx = modes.indexOf(it) })
    OutlinedTextField(a, { if (it.length <= 40) a = it }, label = { Text(mode.aLabel) }, singleLine = true, keyboardOptions = decimal, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(b, { if (it.length <= 40) b = it }, label = { Text(mode.bLabel) }, singleLine = true, keyboardOptions = decimal, modifier = Modifier.fillMaxWidth())

    val x = Parse.double(a)
    val y = Parse.double(b)
    if (a.isBlank() || b.isBlank()) HintText("Fill in both fields.")
    else if (x == null || y == null) ErrorText("Enter valid numbers.")
    else when (mode) {
        PercentMode.OF -> ResultCard("${Fmt.num(x)}% of ${Fmt.num(y)}", Fmt.num(PercentTools.percentOf(x, y)))
        PercentMode.CHANGE -> when (val r = PercentTools.change(x, y)) {
            is Outcome.Ok -> {
                val kind = if (r.value > 0) "Increase" else if (r.value < 0) "Decrease" else "No change"
                ResultCard(kind, "${Fmt.num(r.value, 10)}%")
            }
            is Outcome.Err -> ErrorText(r.message)
        }
        PercentMode.DIFF -> when (val r = PercentTools.difference(x, y)) {
            is Outcome.Ok -> ResultCard("Difference", "${Fmt.num(r.value, 10)}%")
            is Outcome.Err -> ErrorText(r.message)
        }
    }
}
