package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.fillMaxWidth
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
import net.canvoki.vokibot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnumField(
    options: List<SelectableOption>,
    selectedValue: String,
    onValueChanged: (String) -> Unit,
    label: String,
) {
    val selected = options.find { it.value == selectedValue }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value =
                selected?.let {
                    if (it.labelRes != 0) stringResource(it.labelRes) else it.value
                }
                    ?: stringResource(R.string.enum_option_custom_value, selectedValue),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (option.labelRes != 0) {
                                stringResource(option.labelRes)
                            } else {
                                option.value
                            },
                        )
                    },
                    onClick = {
                        onValueChanged(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}
