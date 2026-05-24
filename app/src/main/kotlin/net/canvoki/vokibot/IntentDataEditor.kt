package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IntentDataEditor(
    dataUri: String?,
    mimeType: String?,
    onDataChanged: (String?) -> Unit,
    onMimeChanged: (String?) -> Unit,
) {
    Column {
        SectionHeader("Data")

        OutlinedTextField(
            value = dataUri ?: "",
            onValueChange = { onDataChanged(it.ifBlank { null }) },
            label = { Text("URI") },
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
