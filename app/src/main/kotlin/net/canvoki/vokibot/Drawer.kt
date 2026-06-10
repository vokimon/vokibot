package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.preferences.PreferenceCategory
import net.canvoki.shared.settings.LanguageSettings
import net.canvoki.shared.settings.ThemeSettings
import net.canvoki.shared.storage.rememberOpenFilePicker
import net.canvoki.shared.storage.rememberSaveFilePicker
import net.canvoki.shared.usermessage.UserMessage
import java.time.LocalDate

@Composable
fun Drawer() {
    val context = LocalContext.current
    val repo = FileDataRepository.fromContext(context)
    val saver = rememberSaveFilePicker("application/json")
    val opener = rememberOpenFilePicker()

    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        PreferenceCategory("Settings") {
            LanguageSettings.Preference()
            ThemeSettings.Preference()
        }
        PreferenceCategory("Data Exchange") {
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
            ListItem(
                headlineContent = { Text("Import") },
                supportingContent = { Text("Load automations from a file") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_file_download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier =
                    Modifier.clickable {
                        opener.open(arrayOf("application/json")) { bytes ->
                            bytes?.let {
                                val bundle = ExportedBundle.fromJson(it.decodeToString())
                                repo.importBundle(bundle)
                                UserMessage.Info("Imported ${bundle.entities.size} entities").post()
                            }
                        }
                    },
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
