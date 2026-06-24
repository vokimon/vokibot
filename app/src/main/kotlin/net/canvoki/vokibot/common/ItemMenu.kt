package net.canvoki.vokibot.common

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.canvoki.vokibot.R

@Composable
fun ItemMenu(content: @Composable (onDismiss: () -> Unit) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    val onDismiss = { menuExpanded = false }
    IconButton(onClick = { menuExpanded = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = stringResource(R.string.item_menu_options_desc),
        )
    }
    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = onDismiss,
    ) {
        content(onDismiss)
    }
}
