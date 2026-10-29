package com.offlinekit.app.ui.tools

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.offlinekit.app.core.Outcome
import com.offlinekit.app.domain.PasswordGenerator
import com.offlinekit.app.domain.PasswordOptions
import com.offlinekit.app.domain.TextTools
import com.offlinekit.app.ui.components.ErrorText
import com.offlinekit.app.ui.components.HintText
import com.offlinekit.app.ui.components.ResultCard
import com.offlinekit.app.ui.components.ToggleRow
import kotlin.math.roundToInt

@Composable
fun ColumnScope.TextTool() {
    var text by rememberSaveable { mutableStateOf("") }
    val stats = remember(text) { TextTools.stats(text) }
    val clipboard = LocalClipboardManager.current

    OutlinedTextField(
        text, { if (it.length <= 50_000) text = it }, label = { Text("Text") },
        minLines = 6, modifier = Modifier.fillMaxWidth(),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ResultCard("Characters", "${stats.characters}", copyable = false)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { ResultCard("Words", "${stats.words}", copyable = false) }
        Box(Modifier.weight(1f)) { ResultCard("Lines", "${stats.lines}", copyable = false) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton({ text = text.uppercase() }, Modifier.weight(1f)) { Text("UPPER") }
        OutlinedButton({ text = text.lowercase() }, Modifier.weight(1f)) { Text("lower") }
        OutlinedButton({ text = TextTools.titleCase(text) }, Modifier.weight(1f)) { Text("Title") }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton({ text = text.trim() }, Modifier.weight(1f)) { Text("Trim") }
        OutlinedButton({ clipboard.setText(AnnotatedString(text)) }, Modifier.weight(1f), enabled = text.isNotEmpty()) { Text("Copy") }
        OutlinedButton({ text = "" }, Modifier.weight(1f), enabled = text.isNotEmpty()) { Text("Clear") }
    }
}

@Composable
fun ColumnScope.QrTool() {
    var text by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        text, { if (it.length <= 1000) text = it }, label = { Text("Text or link to encode") },
        minLines = 3, modifier = Modifier.fillMaxWidth(),
    )
    if (text.isBlank()) {
        HintText("Type something to generate a QR code. Everything is generated on this device.")
        return
    }
    val matrix = remember(text) {
        try {
            QRCodeWriter().encode(
                text, BarcodeFormat.QR_CODE, 0, 0,
                mapOf(EncodeHintType.MARGIN to 2, EncodeHintType.CHARACTER_SET to "UTF-8"),
            )
        } catch (e: Exception) { null }
    }
    if (matrix == null) {
        ErrorText("This text is too long to fit in a QR code.")
        return
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.size(280.dp).background(Color.White).semantics { contentDescription = "QR code" },
        ) {
            val n = matrix.width
            val cell = size.width / n
            for (y in 0 until n) for (x in 0 until n) {
                if (matrix.get(x, y)) {
                    drawRect(Color.Black, Offset(x * cell, y * cell), Size(cell + 0.6f, cell + 0.6f))
                }
            }
        }
    }
    HintText("To keep the code, take a screenshot. Direct image export is not part of version 1.")
}

@Composable
fun ColumnScope.PasswordTool() {
    var length by rememberSaveable { mutableStateOf(16) }
    var lower by rememberSaveable { mutableStateOf(true) }
    var upper by rememberSaveable { mutableStateOf(true) }
    var digits by rememberSaveable { mutableStateOf(true) }
    var symbols by rememberSaveable { mutableStateOf(true) }
    // Deliberately `remember`, not `rememberSaveable`: passwords are never written to saved state.
    var result by remember { mutableStateOf<Outcome<String>?>(null) }

    Text("Length: $length")
    Slider(
        value = length.toFloat(), onValueChange = { length = it.roundToInt() },
        valueRange = PasswordGenerator.MIN_LENGTH.toFloat()..PasswordGenerator.MAX_LENGTH.toFloat(),
        steps = PasswordGenerator.MAX_LENGTH - PasswordGenerator.MIN_LENGTH - 1,
    )
    ToggleRow("Lowercase (a–z)", lower) { lower = it }
    ToggleRow("Uppercase (A–Z)", upper) { upper = it }
    ToggleRow("Digits (0–9)", digits) { digits = it }
    ToggleRow("Symbols (!@#…)", symbols) { symbols = it }
    Button({ result = PasswordGenerator.generate(PasswordOptions(length, lower, upper, digits, symbols)) }, Modifier.fillMaxWidth()) {
        Text("Generate")
    }
    when (val r = result) {
        is Outcome.Ok -> ResultCard("Password", r.value)
        is Outcome.Err -> ErrorText(r.message)
        null -> HintText("Passwords are created on this device and are never saved or logged.")
    }
}
