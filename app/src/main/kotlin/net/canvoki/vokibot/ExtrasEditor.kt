package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.serializer

val ExtraValueMapSaver: Saver<Map<String, ExtraValue>, String> =
    Saver(
        save = { JsonConfig.encodeToString(MapSerializer(serializer(), serializer()), it) },
        restore = { JsonConfig.decodeFromString(MapSerializer(serializer(), serializer()), it) },
    )

val ExtraSpecListSaver: Saver<List<ExtraSpec>, String> =
    Saver(
        save = { JsonConfig.encodeToString(it) },
        restore = { JsonConfig.decodeFromString(it) },
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtrasSection(
    specs: List<ExtraSpec>,
    extras: Map<String, ExtraValue>,
    onExtraChanged: (key: String, value: ExtraValue) -> Unit,
    onAddExtra: ((ExtraSpec) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader("Extra parameters")
        specs.forEach { spec ->
            val value = extras[spec.key] ?: spec.defaultValue()
            value.Editor(
                spec = spec,
                onChanged = { newValue -> onExtraChanged(spec.key, newValue) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (onAddExtra != null) {
            var showDialog by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                Spacer(Modifier.size(4.dp))
                Text("Add extra parameter")
            }

            if (showDialog) {
                var newKey by remember { mutableStateOf("") }
                var newType by remember { mutableStateOf(ExtraType.STRING) }
                var typeExpanded by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add extra parameter") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newKey,
                                onValueChange = { newKey = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = typeExpanded,
                                onExpandedChange = { typeExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = newType.displayName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Type") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = typeExpanded,
                                        )
                                    },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                )
                                ExposedDropdownMenu(
                                    expanded = typeExpanded,
                                    onDismissRequest = { typeExpanded = false },
                                ) {
                                    ExtraType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.displayName) },
                                            onClick = {
                                                newType = type
                                                typeExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onAddExtra(ExtraSpec(key = newKey, type = newType, label = newKey))
                                showDialog = false
                            },
                            enabled = newKey.isNotBlank(),
                        ) { Text("Add") }
                    },
                    dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
                )
            }
        }
    }
}
