package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data class ShortcutTriggerEditor(
    val triggerId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }

        var name by rememberSaveable { mutableStateOf("") }
        var isProcessing by remember { mutableStateOf(false) }
        var existingTrigger by remember { mutableStateOf<ShortcutTrigger?>(null) }

        LaunchedEffect(triggerId) {
            if (triggerId != null) {
                existingTrigger = repository.trigger.all().find { it.id == triggerId } as? ShortcutTrigger
                existingTrigger?.let { name = it.displayName }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text =
                    stringResource(
                        if (triggerId == null) {
                            R.string.shortcut_editor_create_title
                        } else {
                            R.string.shortcut_editor_edit_title
                        },
                    ),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.shortcut_editor_name_label)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    isProcessing = true
                    if (triggerId != null && existingTrigger != null) {
                        val updated = existingTrigger!!.copy(displayName = name.trim())
                        repository.trigger.save(updated)
                        updated.update(context)
                    } else {
                        val new = ShortcutTrigger(displayName = name.trim())
                        repository.trigger.save(new)
                        new.pin(context)
                    }
                    isProcessing = false
                    nav.pop()
                },
                enabled = name.isNotBlank() && !isProcessing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text =
                        stringResource(
                            if (triggerId == null) R.string.shortcut_editor_create else R.string.shortcut_editor_save,
                        ),
                )
            }
        }
    }
}
