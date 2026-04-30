package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data class ShortcutTriggerEditor(
    val triggerId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }

        var name by rememberSaveable { mutableStateOf("") }
        var isDirty by remember { mutableStateOf(false) }
        var isProcessing by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }
        var hasLoaded by remember { mutableStateOf(false) }

        LaunchedEffect(triggerId) {
            if (triggerId != null && !hasLoaded) {
                val existingTrigger = repository.trigger.all().find { it.id == triggerId } as? ShortcutTrigger
                existingTrigger?.let { name = it.displayName }
                hasLoaded = true
            }
        }

        LaunchedEffect(isDirty) {
            nav.onBack(this@ShortcutTriggerEditor, enabled = isDirty) {
                showDiscardDialog = true
            }
        }
        ConfirmDialog(
            show = showDiscardDialog,
            title = stringResource(R.string.shortcut_editor_discard_title),
            text = stringResource(R.string.shortcut_editor_discard_message),
            confirmText = stringResource(R.string.shortcut_editor_discard_confirm),
            dismissText = stringResource(R.string.shortcut_editor_discard_cancel),
            onDismiss = {
                showDiscardDialog = false
            },
            onConfirm = {
                showDiscardDialog = false
                nav.pop()
            },
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                title = stringResource(R.string.shortcut_editor_title),
                icon = painterResource(R.drawable.ic_shortcut),
                actionText = stringResource(R.string.shortcut_editor_save),
                action = {
                    if (name.isBlank()) return@EditorHeader
                    isProcessing = true
                    val trigger =
                        if (triggerId != null) {
                            ShortcutTrigger(
                                id = triggerId,
                                displayName = name.trim(),
                            )
                        } else {
                            ShortcutTrigger(
                                displayName = name.trim(),
                            )
                        }
                    repository.trigger.save(trigger)

                    if (trigger.isPinned(context)) {
                        trigger.update(context)
                    } else {
                        trigger.pin(context)
                    }

                    isProcessing = false
                    nav.pop()
                },
                actionEnabled = name.isNotBlank() && !isProcessing,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.shortcut_editor_name_label)) },
                placeholder = { Text(stringResource(R.string.shortcut_editor_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}
