package net.canvoki.vokibot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption
import net.canvoki.shared.component.ContextualHelpButton
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.usermessage.UserMessage
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.toPainter

@Serializable
data object CommandList : StackedScreen<String>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        CommandList(nav)
    }
}

@Composable
fun CommandList(
    nav: StackNavigatorState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FileDataRepository.fromContext(context) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    var commandToDelete by remember { mutableStateOf<String?>(null) }

    val fallbacErrorMessage = stringResource(R.string.command_run_error_fallback)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                icon = painterResource(Command.iconRes),
                title = stringResource(R.string.commandlist_header),
            )
            AsyncList(
                refreshKeys = listOf(refreshCounter),
                loader = { repository.loadAllCommands() },
                itemKey = { it.id },
                groupBy = { command -> command.type },
                headerContent = { key: String ->
                    CommandGroupHeader(key)
                },
                notFoundMessage = stringResource(R.string.commandlist_not_found),
            ) { command ->
                var menuExpanded by remember { mutableStateOf(false) }

                val componentIcon = remember(command.id) { command.loadIcon(context) }
                val iconPainter = remember(componentIcon) { componentIcon.toPainter() }

                ListItem(
                    headlineContent = { Text(command.title) },
                    supportingContent = {
                        Text(
                            text = command.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.clickable { nav.pop(command.id) },
                    leadingContent = {
                        Image(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.commandlist_options_desc),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.commandlist_item_menu_edit)) },
                                leadingIcon = {
                                    Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    StorableEntity.getEditorScreen(command.type, command.id)?.let {
                                        nav.push(it) { refreshCounter++ }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(stringResource(R.string.commandlist_run))
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play_arrow),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    scope.launch {
                                        try {
                                            command.execute(context)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            UserMessage.Info(e.message ?: fallbacErrorMessage).post()
                                        }
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.commandlist_remove)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    commandToDelete = command.id
                                },
                            )
                        }
                    },
                )
            }
        }

        FloatingActionButton(
            onClick = { nav.push(CommandTypePicker) { refreshCounter++ } },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.commandlist_create_fab_desc),
            )
        }
    }

    ConfirmDialog(
        show = commandToDelete != null,
        title = stringResource(R.string.commandlist_delete_title),
        text = stringResource(R.string.commandlist_delete_message),
        confirmText = stringResource(R.string.commandlist_delete),
        dismissText = stringResource(R.string.commandlist_cancel),
        onDismiss = {
            commandToDelete = null
        },
        onConfirm = {
            commandToDelete?.let { id ->
                repository.removeCommand(id)
                refreshCounter++
                commandToDelete = null
            }
        },
    )
}

/**
 * Renders a translated, styled header for a command type group.
 * Uses StorableEntity.label to get localized type name from registry.
 */
@Composable
private fun CommandGroupHeader(key: String) {
    val verticalPadding = 8.dp
    val helpResId = StorableEntity.helpResId(key)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = StorableEntity.label(key),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = verticalPadding),
        )
        if (helpResId != 0) {
            ContextualHelpButton(
                title = StorableEntity.label(key),
                description = stringResource(helpResId),
                modifier = Modifier.padding(vertical = verticalPadding),
            )
        }
    }
}
