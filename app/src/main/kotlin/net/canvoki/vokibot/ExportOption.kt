package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.storage.rememberSaveFilePicker
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun ExportOption() {
    val context = LocalContext.current
    val repo = FileDataRepository.fromContext(context)
    val saver = rememberSaveFilePicker("application/json")
    ListItem(
        headlineContent = { Text(stringResource(R.string.dasher_export_title)) },
        supportingContent = { Text(stringResource(R.string.dasher_export_description)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_file_upload),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier =
            Modifier.clickable {
                val timestamp =
                    LocalDateTime
                        .now(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .replace(":", "-")
                val filename = "vokibot-$timestamp.json"
                val json = repo.exportBundle().toJson()
                saver.save(filename, json.toByteArray())
            },
    )
}
