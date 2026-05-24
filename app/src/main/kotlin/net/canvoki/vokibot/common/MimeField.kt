package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.TextFieldValue
import net.canvoki.vokibot.R

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
fun MimeField(
    mimeType: String?,
    onMimeChanged: (String?) -> Unit,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(mimeType) {
        val newText = mimeType.orEmpty()
        if (newText != fieldValue.text) {
            fieldValue = TextFieldValue(newText, TextRange(newText.length))
        }
    }

    var expanded by remember { mutableStateOf(false) }
    val suggestions =
        remember(fieldValue.text) {
            val slash = fieldValue.text.indexOf('/')
            if (slash == -1) {
                mimeDefinitions
                    .map { it.first }
                    .filter { it.startsWith(fieldValue.text, ignoreCase = true) }
            } else {
                val group = fieldValue.text.substring(0, slash + 1)
                val subtype = fieldValue.text.substring(slash + 1)
                mimeDefinitions
                    .firstOrNull { it.first.equals(group, ignoreCase = true) }
                    ?.second
                    ?.filter { it.startsWith(subtype, ignoreCase = true) }
                    ?.map { group + it }
                    ?: emptyList()
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onMimeChanged(it.text.ifBlank { null })
            },
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
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        fieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                        onMimeChanged(suggestion)
                        expanded = false
                    },
                )
            }
        }
    }
}
