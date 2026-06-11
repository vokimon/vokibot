package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import net.canvoki.shared.storage.rememberOpenFilePicker
import net.canvoki.shared.usermessage.UserMessage

@Composable
fun ImportOption() {
    val context = LocalContext.current
    val repo = FileDataRepository.fromContext(context)
    val opener = rememberOpenFilePicker()
    var pendingSummary by remember { mutableStateOf<String?>(null) }
    var pendingBundle by remember { mutableStateOf<ExportedBundle?>(null) }

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
                        try {
                            val bundle = ExportedBundle.fromJson(it.decodeToString())
                            val summary = bundle.analyzeImport(repo.entityIds()).summary()
                            if (summary.isEmpty()) {
                                repo.importBundle(bundle)
                                UserMessage.Info("Imported ${bundle.entities.size} entities").post()
                            } else {
                                pendingSummary = summary
                                pendingBundle = bundle
                            }
                        } catch (e: kotlinx.serialization.SerializationException) {
                            UserMessage.Info("Invalid import file $e").post()
                        }
                    }
                }
            },
    )

    if (pendingSummary != null) {
        ConfirmDialog(
            show = true,
            title = "Import Analysis",
            text = pendingSummary!!,
            confirmText = "Import",
            dismissText = "Cancel",
            onConfirm = {
                pendingBundle?.let { repo.importBundle(it) }
                UserMessage.Info("Imported ${pendingBundle?.entities?.size} entities").post()
                pendingSummary = null
                pendingBundle = null
            },
            onDismiss = {
                pendingSummary = null
                pendingBundle = null
            },
        )
    }
}
