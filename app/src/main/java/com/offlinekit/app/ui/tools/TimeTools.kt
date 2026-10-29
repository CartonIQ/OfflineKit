package com.offlinekit.app.ui.tools

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.offlinekit.app.core.Fmt
import com.offlinekit.app.core.Outcome
import com.offlinekit.app.core.Parse
import com.offlinekit.app.domain.DateTools
import com.offlinekit.app.domain.TimeConverter
import com.offlinekit.app.domain.TimeUnitKind
import com.offlinekit.app.ui.components.DateField
import com.offlinekit.app.ui.components.ErrorText
import com.offlinekit.app.ui.components.HintText
import com.offlinekit.app.ui.components.Picker
import com.offlinekit.app.ui.components.ResultCard
import com.offlinekit.app.ui.components.ToggleRow
import java.time.LocalDate
import java.time.Period

private fun Period.text(): String = "$years years, $months months, $days days"

@Composable
fun ColumnScope.DateDiffTool() {
    var start by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var inclusive by rememberSaveable { mutableStateOf(false) }
    val a = LocalDate.ofEpochDay(start)
    val b = LocalDate.ofEpochDay(end)

    DateField("Start", start, { start = it })
    DateField("End", end, { end = it })
    ToggleRow("Count both end dates (inclusive)", inclusive) { inclusive = it }
    val days = DateTools.daysBetween(a, b, inclusive)
    ResultCard("Difference", "$days day" + if (days == 1L) "" else "s")
    HintText("= ${DateTools.breakdown(a, b).text()}" + if (a.isAfter(b)) " (start is after end)" else "")
}

@Composable
fun ColumnScope.AgeTool() {
    var birth by rememberSaveable { mutableStateOf(LocalDate.of(2000, 1, 1).toEpochDay()) }
    DateField("Birth date", birth, { birth = it })
    val today = LocalDate.now()
    when (val r = DateTools.age(LocalDate.ofEpochDay(birth), today)) {
        is Outcome.Ok -> {
            ResultCard("Age today ($today)", r.value.text())
        }
        is Outcome.Err -> ErrorText(r.message)
    }
}

@Composable
fun ColumnScope.TimeConverterTool() {
    val units = TimeUnitKind.entries
    var input by rememberSaveable { mutableStateOf("") }
    var fromIdx by rememberSaveable { mutableStateOf(2) }
    val from = units[fromIdx]

    OutlinedTextField(
        input, { if (it.length <= 40) input = it }, label = { Text("Value") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(),
    )
    Picker("Convert from", units, from, { it.label }, { fromIdx = units.indexOf(it) })

    if (input.isBlank()) HintText("Enter a value.")
    else {
        val v = Parse.decimal(input)
        if (v == null) ErrorText("Enter a valid number.")
        else units.filter { it != from }.forEach { to ->
            when (val r = TimeConverter.convert(v, from, to)) {
                is Outcome.Ok -> ResultCard(to.label, Fmt.num(r.value, 12))
                is Outcome.Err -> ErrorText(r.message)
            }
        }
    }
}
