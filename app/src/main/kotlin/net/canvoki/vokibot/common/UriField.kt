package net.canvoki.vokibot.common

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import net.canvoki.shared.storage.rememberFileUriPicker
import net.canvoki.vokibot.R

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

@Composable
fun UriField(
    uri: String?,
    onUriChanged: (String?) -> Unit,
    label: String = "URI",
    allowedSchemes: List<String>? = null,
    onFilePicked: ((Uri) -> Unit)? = null,
) {
    val showPicker = uri.isNullOrBlank()
    val keyboardType =
        when {
            uri?.startsWith("tel:") == true -> KeyboardType.Phone
            uri?.startsWith("smsto:") == true -> KeyboardType.Phone
            uri?.startsWith("mailto:") == true -> KeyboardType.Email
            uri?.startsWith("geo:") == true -> KeyboardType.Decimal
            else -> KeyboardType.Uri
        }

    val pickFile = rememberFileUriPicker()
    val context = LocalContext.current
    var resolvedName by remember { mutableStateOf<String?>(null) }

    val textToShow = resolvedName ?: uri.orEmpty()
    var fieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(uri, resolvedName) {
        val newText = textToShow
        if (newText != fieldValue.text) {
            fieldValue = TextFieldValue(newText, TextRange(newText.length))
        }
    }

    LaunchedEffect(uri) {
        resolvedName = null
        if (uri?.startsWith("content://") == true) {
            resolvedName = resolveDisplayName(context, Uri.parse(uri))
        }
    }

    val isReadOnly = resolvedName != null

    val effectiveSchemes =
        allowedSchemes?.let { allowed ->
            schemes.filter { scheme -> allowed.any { scheme.startsWith(it) } }
        } ?: schemes

    var expanded by remember { mutableStateOf(false) }
    val suggestions =
        remember(fieldValue.text, effectiveSchemes) {
            effectiveSchemes.filter { it.startsWith(fieldValue.text, ignoreCase = true) }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            readOnly = isReadOnly,
            value = fieldValue,
            onValueChange = {
                if (!isReadOnly) {
                    fieldValue = it
                    onUriChanged(it.text.ifBlank { null })
                }
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            trailingIcon = {
                Row {
                    if (showPicker) {
                        IconButton(onClick = {
                            pickFile(arrayOf("*/*")) { pickedUri ->
                                if (pickedUri != null) {
                                    val uriString = pickedUri.toString()
                                    onUriChanged(uriString)
                                    onFilePicked?.invoke(pickedUri)
                                }
                            }
                        }) {
                            Icon(painterResource(R.drawable.ic_folder), contentDescription = null)
                        }
                    }
                    if (uri != null) {
                        IconButton(onClick = {
                            resolvedName = null
                            onUriChanged(null)
                        }) {
                            Icon(painterResource(R.drawable.ic_close), contentDescription = null)
                        }
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
                        onUriChanged(suggestion)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun resolveDisplayName(
    context: Context,
    uri: Uri,
): String? =
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0) cursor.getString(nameIdx) else null
        } else {
            null
        }
    }
