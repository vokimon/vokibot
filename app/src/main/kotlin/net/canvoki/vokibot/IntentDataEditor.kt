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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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

    var uriExpanded by remember { mutableStateOf(false) }
    val uriText = dataUri ?: ""
    val uriSuggestions =
        remember(uriText) {
            schemes.filter { it.startsWith(uriText, ignoreCase = true) }
        }

    var mimeExpanded by remember { mutableStateOf(false) }
    val mimeText = mimeType ?: ""
    val mimeSuggestions =
        remember(mimeText) {
            val slash = mimeText.indexOf('/')
            if (slash == -1) {
                mimeDefinitions
                    .map { it.first }
                    .filter { it.startsWith(mimeText, ignoreCase = true) }
            } else {
                val group = mimeText.substring(0, slash + 1)
                val subtype = mimeText.substring(slash + 1)
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
                value = uriText,
                onValueChange = { onDataChanged(it.ifBlank { null }) },
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
                value = mimeText,
                onValueChange = { onMimeChanged(it.ifBlank { null }) },
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
                            onMimeChanged(suggestion)
                            mimeExpanded = false
                        },
                    )
                }
            }
        }
    }
}
