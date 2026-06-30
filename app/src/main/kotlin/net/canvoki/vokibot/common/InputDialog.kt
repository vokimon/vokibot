package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun InputDialog(
    show: Boolean,
    title: String,
    label: String,
    value: String = "",
    placeholder: String? = null,
    confirmText: String,
    dismissText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var inputValue by remember { mutableStateOf(value) }

    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text(label) },
                    placeholder = placeholder?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(inputValue) },
                enabled = inputValue.isNotBlank(),
            ) {
                Text(confirmText, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}
