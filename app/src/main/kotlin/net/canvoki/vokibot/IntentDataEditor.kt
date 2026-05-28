package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.common.MimeField
import net.canvoki.vokibot.common.SectionHeader
import net.canvoki.vokibot.common.UriField

@Composable
fun IntentDataEditor(
    dataUri: String?,
    mimeType: String?,
    showMime: Boolean = true,
    onDataChanged: (String?) -> Unit,
    onMimeChanged: (String?) -> Unit,
    dataUriRequired: Boolean = false,
    allowedSchemes: List<String>? = null,
) {
    val context = LocalContext.current
    Column {
        SectionHeader(stringResource(R.string.intent_data_editor_data_header))
        UriField(
            uri = dataUri,
            onUriChanged = onDataChanged,
            label =
                if (dataUriRequired) {
                    stringResource(
                        R.string.intent_data_editor_data_uri_required,
                    )
                } else {
                    stringResource(R.string.intent_data_editor_data_uri)
                },
            allowedSchemes = allowedSchemes,
            onFilePicked = { uri ->
                context.contentResolver.getType(uri)?.let { onMimeChanged(it) }
            },
        )
        Spacer(Modifier.height(8.dp))
        if (showMime) {
            MimeField(
                mimeType = mimeType,
                onMimeChanged = onMimeChanged,
            )
        }
    }
}
