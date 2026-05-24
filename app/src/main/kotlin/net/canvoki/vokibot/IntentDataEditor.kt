package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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

    Column {
        SectionHeader("Data")

        OutlinedTextField(
            value = dataUri ?: "",
            onValueChange = { onDataChanged(it.ifBlank { null }) },
            label = { Text("URI") },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = mimeType ?: "",
            onValueChange = { onMimeChanged(it.ifBlank { null }) },
            label = { Text("MIME type") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
