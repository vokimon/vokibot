package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun IntentActionSelector(
    supportedActions: List<ActionDefinition>,
    onSelected: (ActionDefinition?) -> Unit,
    onCustomChanged: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    var selected by remember { mutableStateOf<ActionDefinition?>(null) }
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

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(12.dp),
        ) {
            Text(
                text = selected?.label ?: stringResource(R.string.intent_editor_custom_or_none),
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_drop_down),
                contentDescription = null,
            )
        }

        DropdownMenu(
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
