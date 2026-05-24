package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

private val schemes =
    listOf(
        "https://",
        "http://",
        "content://",
        "tel:",
        "smsto:",
        "mailto:",
        "geo:",
        "market://",
    )

private val mimeDefinitions =
    listOf(
        "image/" to listOf("jpeg", "png", "gif", "webp", "svg+xml", "bmp", "x-icon"),
        "video/" to listOf("mp4", "mpeg", "webm", "ogg", "3gpp", "x-matroska"),
        "audio/" to listOf("mpeg", "ogg", "wav", "webm", "aac", "flac"),
        "text/" to listOf("plain", "html", "css", "javascript", "csv", "xml", "markdown"),
        "application/" to listOf("json", "xml", "pdf", "zip", "octet-stream"),
        "multipart/" to listOf("form-data", "mixed", "alternative"),
        "message/" to listOf("rfc822"),
        "model/" to listOf("gltf+json", "obj"),
        "font/" to listOf("ttf", "otf", "woff", "woff2"),
    )

@Composable
fun IntentDataEditor(
    dataUri: String?,
    mimeType: String?,
    onDataChanged: (String?) -> Unit,
    onMimeChanged: (String?) -> Unit,
) {
    val keyboardType =
        when {
            dataUri?.startsWith("tel:") == true -> KeyboardType.Phone
            dataUri?.startsWith("smsto:") == true -> KeyboardType.Phone
            dataUri?.startsWith("mailto:") == true -> KeyboardType.Email
            dataUri?.startsWith("geo:") == true -> KeyboardType.Decimal
            else -> KeyboardType.Uri
        }

    // Use TextFieldValue to preserve cursor position during typing.
    // The LaunchedEffect only syncs when the external dataUri changes
    // (e.g. clear button resets to null -> "").
    // When the user types, dataUri already matches the text, so the effect is a no-op
    // and the cursor stays where the user put it.
    var uriFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(dataUri) {
        val newText = dataUri.orEmpty()
        if (newText != uriFieldValue.text) {
            uriFieldValue = TextFieldValue(newText, TextRange(newText.length))
        }
    }

    var uriExpanded by remember { mutableStateOf(false) }
    val uriSuggestions =
        remember(uriFieldValue.text) {
            schemes.filter { it.startsWith(uriFieldValue.text, ignoreCase = true) }
        }

    // Same pattern for MIME type.
    var mimeFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(mimeType) {
        val newText = mimeType.orEmpty()
        if (newText != mimeFieldValue.text) {
            mimeFieldValue = TextFieldValue(newText, TextRange(newText.length))
        }
    }

    var mimeExpanded by remember { mutableStateOf(false) }
    val mimeSuggestions =
        remember(mimeFieldValue.text) {
            val slash = mimeFieldValue.text.indexOf('/')
            if (slash == -1) {
                mimeDefinitions
                    .map { it.first }
                    .filter { it.startsWith(mimeFieldValue.text, ignoreCase = true) }
            } else {
                val group = mimeFieldValue.text.substring(0, slash + 1)
                val subtype = mimeFieldValue.text.substring(slash + 1)
                mimeDefinitions
                    .firstOrNull { it.first.equals(group, ignoreCase = true) }
                    ?.second
                    ?.filter { it.startsWith(subtype, ignoreCase = true) }
                    ?.map { group + it }
                    ?: emptyList()
            }
        }

    Column {
        SectionHeader("Data")

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = uriExpanded,
            onExpandedChange = { uriExpanded = it },
        ) {
            OutlinedTextField(
                value = uriFieldValue,
                onValueChange = { uriFieldValue = it; onDataChanged(it.text.ifBlank { null }) },
                label = { Text("URI") },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                trailingIcon = {
                    if (dataUri != null) {
                        IconButton(onClick = { onDataChanged(null) }) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = uriExpanded,
                onDismissRequest = { uriExpanded = false },
            ) {
                uriSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            // Move cursor to end after autocompletion so the user can keep typing.
                            uriFieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                            onDataChanged(suggestion)
                            uriExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = mimeExpanded,
            onExpandedChange = { mimeExpanded = it },
        ) {
            OutlinedTextField(
                value = mimeFieldValue,
                onValueChange = { mimeFieldValue = it; onMimeChanged(it.text.ifBlank { null }) },
                label = { Text("MIME type") },
                trailingIcon = {
                    if (mimeType != null) {
                        IconButton(onClick = { onMimeChanged(null) }) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = null)
                        }
                    }
                },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = mimeExpanded,
                onDismissRequest = { mimeExpanded = false },
            ) {
                mimeSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            mimeFieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                            onMimeChanged(suggestion)
                            mimeExpanded = false
                        },
                    )
                }
            }
        }
    }
}
