package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val filtered =
        remember(uriText) {
            schemes.filter { it.startsWith(uriText, ignoreCase = true) && it != uriText }
        }
    LaunchedEffect(filtered) { uriExpanded = filtered.isNotEmpty() }

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
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = uriExpanded,
                onDismissRequest = { uriExpanded = false },
            ) {
                filtered.forEach { suggestion ->
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
        OutlinedTextField(
            value = mimeType ?: "",
            onValueChange = { onMimeChanged(it.ifBlank { null }) },
            label = { Text("MIME type") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
