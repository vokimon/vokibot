package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import net.canvoki.vokibot.R

@Composable
fun FreeStringSetField(
    values: List<String>,
    onValuesChanged: (List<String>) -> Unit,
    label: String,
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            values.forEach { value ->
                InputChip(
                    selected = true,
                    onClick = { },
                    label = { Text(value) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onValuesChanged(values - value) },
                            modifier = Modifier.size(InputChipDefaults.AvatarSize),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = "Remove",
                            )
                        }
                    },
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Add value") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) {
                            onValuesChanged(values + listOf(trimmed))
                            text = ""
                        }
                    },
                ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
