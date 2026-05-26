package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun IntentActionSelector(
    supportedActions: List<ActionDefinition>,
    selectedAction: ActionDefinition? = null,
    onSelected: (ActionDefinition?) -> Unit,
    onCustomChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    var selected by remember(selectedAction) { mutableStateOf(selectedAction) }
    var custom by remember { mutableStateOf("") }

    val actionsToShow =
        if (supportedActions.isNotEmpty()) {
            supportedActions
        } else {
            StandardActions.all()
        }

    LaunchedEffect(actionsToShow) {
        if (selected == null) {
            selected = actionsToShow.firstOrNull()
            onSelected(selected)
        }
    }

    Column {
        SectionHeader(stringResource(R.string.intent_editor_action_label))

        Spacer(modifier = Modifier.height(8.dp))

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selected?.label ?: stringResource(R.string.intent_editor_custom_or_none),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.intent_editor_action_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                actionsToShow.forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        onClick = {
                            selected = action
                            expanded = false
                            onSelected(action)
                        },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.intent_editor_custom_or_none)) },
                    onClick = {
                        selected = null
                        expanded = false
                        onSelected(null)
                    },
                )
            }
        }

        if (selected == null) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = custom,
                onValueChange = {
                    custom = it
                    onCustomChanged(it)
                },
                label = { Text(stringResource(R.string.intent_editor_action_string_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
