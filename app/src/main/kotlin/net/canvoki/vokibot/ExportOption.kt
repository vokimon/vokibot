package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import net.canvoki.shared.storage.rememberSaveFilePicker
import java.time.LocalDate

@Composable
fun ExportOption(repo: FileDataRepository) {
    val saver = rememberSaveFilePicker("application/json")
    ListItem(
        headlineContent = { Text("Export") },
        supportingContent = { Text("Save automations to a file") },
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_file_upload),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier =
            Modifier.clickable {
                val date = LocalDate.now()
                val filename = "vokibot-$date.vokibot.json"
                val json = repo.exportBundle().toJson()
                saver.save(filename, json.toByteArray())
            },
    )
}
