package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable

@Composable
fun IntentDataEditor(
    dataUri: String?,
    mimeType: String?,
    onDataChanged: (String?) -> Unit,
    onMimeChanged: (String?) -> Unit,
) {
    Column {
        SectionHeader("Data")
    }
}
