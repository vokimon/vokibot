package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.common.MimeField
import net.canvoki.vokibot.common.UriField

@Composable
fun IntentDataEditor(
    dataUri: String?,
    mimeType: String?,
    onDataChanged: (String?) -> Unit,
    onMimeChanged: (String?) -> Unit,
    dataUriRequired: Boolean = false,
    allowedSchemes: List<String>? = null,
) {
    val context = LocalContext.current
    Column {
        SectionHeader("Data")
        UriField(
            uri = dataUri,
            onUriChanged = onDataChanged,
            label = if (dataUriRequired) "URI (required)" else "URI",
            allowedSchemes = allowedSchemes,
            onFilePicked = { uri ->
                context.contentResolver.getType(uri)?.let { onMimeChanged(it) }
            },
        )
        Spacer(Modifier.height(8.dp))
        MimeField(
            mimeType = mimeType,
            onMimeChanged = onMimeChanged,
        )
    }
}
