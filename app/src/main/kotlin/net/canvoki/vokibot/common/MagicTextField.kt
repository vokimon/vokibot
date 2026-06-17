package net.canvoki.vokibot.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import net.canvoki.vokibot.R

@Composable
fun MagicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onMagicClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        trailingIcon = {
            if (value.isEmpty()) {
                IconButton(onClick = onMagicClick) {
                    Icon(painterResource(R.drawable.ic_auto_awesome), contentDescription = null)
                }
            } else {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = null)
                }
            }
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        modifier = modifier,
    )
}
