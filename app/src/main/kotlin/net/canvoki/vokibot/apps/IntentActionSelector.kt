package net.canvoki.vokibot.apps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.R
import net.canvoki.vokibot.common.SectionHeader

@Composable
fun IntentActionSelector(
    selectableActions: List<ActionDefinition>,
    action: String?,
    onActionChanged: (String?) -> Unit,
) {
    val selected = selectableActions.find { it.action == action }

    Column {
        SectionHeader(stringResource(R.string.intent_editor_action_label))

        if (selectableActions.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(8.dp))

            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value =
                        selected?.let { stringResource(it.labelRes) }
                            ?: stringResource(R.string.intent_editor_custom_or_none),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.intent_editor_action_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier =
                        Modifier
                            .menuAnchor(
                                type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            ).fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    selectableActions.forEach { actionDef ->
                        DropdownMenuItem(
                            text = { Text(stringResource(actionDef.labelRes)) },
                            onClick = {
                                expanded = false
                                onActionChanged(actionDef.action)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.intent_editor_custom_or_none)) },
                        onClick = {
                            expanded = false
                            onActionChanged(null)
                        },
                    )
                }
            }
        }

        if (selected == null) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = action ?: "",
                onValueChange = {
                    onActionChanged(it)
                },
                label = { Text(stringResource(R.string.intent_editor_action_string_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
