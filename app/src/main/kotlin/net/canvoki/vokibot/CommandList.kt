package net.canvoki.vokibot

import android.content.ComponentName
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption

@Composable
fun CommandList(nav: ScreenNavigator) {
    CommandList(
        onLaunchAppSelected = {
            nav.push(BuilderScreen.AppList)
        },
        onCommandSelected = { commandId ->
            nav.back(commandId)
        },
    )
}

@Composable
fun CommandList(
    onLaunchAppSelected: () -> Unit,
    onCommandSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { FileDataRepository.fromContext(context) }
    var showTypeChooser by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    var commandToDelete by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncList(
            refreshKeys = listOf(refreshCounter),
            loader = { repository.loadAllCommands() },
            itemKey = { it.id },
            groupBy = { command -> command.typeLabelRes.toString() },
            headerContent = { key: String ->
                CommandGroupHeader(key)
            },
            notFoundMessage = stringResource(R.string.commandlist_not_found),
        ) { command ->
            var menuExpanded by remember { mutableStateOf(false) }

            // Get component icon (only for LaunchActivityCommand)
            val componentIcon =
                remember(command.packageName) {
                    runCatching {
                        val pm = context.packageManager
                        val className = (command as? LaunchActivityCommand)?.className
                        if (className != null) {
                            val componentName = ComponentName(command.packageName, className)
                            pm.getActivityIcon(componentName)
                        } else {
                            pm.getApplicationIcon(command.packageName)
                        }
                    }.getOrNull()
                }

            ListItem(
                headlineContent = { Text(command.displayName) },
                supportingContent = {
                    Text(
                        text = "${command.packageName}/${(command as? LaunchActivityCommand)?.className ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                    )
                },
                modifier = Modifier.clickable { onCommandSelected(command.id) },
                leadingContent = {
                    componentIcon?.let { icon ->
                        Image(
                            painter = BitmapPainter(icon.toBitmap().asImageBitmap()),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    } ?: Icon(
                        painter = painterResource(R.drawable.ic_brand),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

        FloatingActionButton(
            onClick = { showTypeChooser = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.commandlist_create_fab_desc),
            )
        }
    }

    if (commandToDelete != null) {
        AlertDialog(
            onDismissRequest = { commandToDelete = null },
            title = { Text(stringResource(R.string.commandlist_delete_title)) },
            text = { Text(stringResource(R.string.commandlist_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        commandToDelete?.let { id ->
                            repository.removeCommand(id)
                            refreshCounter++
                            commandToDelete = null
                        }
                    },
                ) {
                    Text(stringResource(R.string.commandlist_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { commandToDelete = null }) {
                    Text(stringResource(R.string.commandlist_cancel))
                }
            },
        )
    }

    if (showTypeChooser) {
        ChooserDialog(
            title = stringResource(R.string.commandlist_create_command_title),
            options =
                listOf(
                    ChooserOption(
                        value = "application",
                        label = stringResource(R.string.commandlist_launch_app_option),
                    ),
                ),
            selectedValue = "",
            onConfirm = { value ->
                showTypeChooser = false
                if (value == "application") onLaunchAppSelected()
            },
            onDismiss = { showTypeChooser = false },
        )
    }
}

/**
 * Renders a translated, styled header for a command type group.
 * Converts the string key back to Int @StringRes for translation.
 */
@Composable
private fun CommandGroupHeader(key: String) {
    val resId = key.toInt()
    Text(
        text = stringResource(resId),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
