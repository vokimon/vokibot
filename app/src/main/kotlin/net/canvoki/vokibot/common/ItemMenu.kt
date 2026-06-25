package net.canvoki.vokibot.common

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.vokibot.ConfirmDialog
import net.canvoki.vokibot.R
import net.canvoki.vokibot.StorableEntity

@Composable
fun ItemMenu(content: @Composable (onDismiss: () -> Unit, onConfirm: (String, () -> Unit) -> Unit) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var confirmationMessage by remember { mutableStateOf("") }
    val onDismiss = { menuExpanded = false }
    val onConfirm = { message: String, action: () -> Unit ->
        menuExpanded = false
        confirmationMessage = message
        confirmAction = action
        showConfirmDialog = true
    }
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
        content(onDismiss, onConfirm)
    }

    ConfirmDialog(
        show = showConfirmDialog,
        title = confirmationMessage,
        text = stringResource(R.string.item_menu_delete_confirmation_message),
        confirmText = stringResource(R.string.item_menu_delete),
        dismissText = stringResource(R.string.item_menu_delete_confirmation_cancel),
        onDismiss = { showConfirmDialog = false },
        onConfirm = {
            confirmAction?.invoke()
            showConfirmDialog = false
        },
    )
}

@Composable
fun ItemMenuEditOption(
    type: String,
    id: String,
    nav: StackNavigatorState,
    onDismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.item_menu_edit)) },
        leadingIcon = { Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null) },
        onClick = {
            onDismiss()
            StorableEntity.getEditorScreen(type, id)?.let {
                nav.push(it)
            }
        },
    )
}

@Composable
fun ItemMenuRunOption(
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.item_menu_run)) },
        leadingIcon = { Icon(painter = painterResource(R.drawable.ic_play_arrow), contentDescription = null) },
        onClick = {
            onDismiss()
            onClick()
        },
    )
}

@Composable
fun ItemMenuDeleteOption(
    confirmationMessage: String,
    onDismiss: () -> Unit,
    onConfirm: (String, () -> Unit) -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.item_menu_delete)) },
        leadingIcon = { Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = null) },
        onClick = {
            onDismiss()
            onConfirm(confirmationMessage, onDelete)
        },
    )
}
