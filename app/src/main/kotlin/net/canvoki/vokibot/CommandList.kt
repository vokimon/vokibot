package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.ItemMenu
import net.canvoki.vokibot.common.ItemMenuDeleteOption
import net.canvoki.vokibot.common.ItemMenuEditOption
import net.canvoki.vokibot.common.ItemMenuRunOption
import net.canvoki.vokibot.common.ListFab
import net.canvoki.vokibot.common.ListGroupHeader
import net.canvoki.vokibot.common.tintIfFlat
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
    val dataVersion = repository.rememberDataVersion()

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
                refreshKeys = listOf(dataVersion),
                loader = { repository.loadAllCommands() },
                itemKey = { it.id },
                groupBy = { command -> command.type },
                headerContent = { key: String ->
                    val helpResId = StorableEntity.helpResId(key)
                    ListGroupHeader(
                        title = StorableEntity.label(key),
                        helpDescription = helpResId.takeIf { it != 0 }?.let { stringResource(it) },
                    )
                },
                notFoundMessage = stringResource(R.string.commandlist_not_found),
            ) { command ->
                val componentIcon = remember(command.id) { command.loadIcon(context) }
                val iconPainter = remember(componentIcon) { componentIcon.toPainter() }
                val tint = componentIcon.tintIfFlat()

                ListItem(
                    headlineContent = { Text(command.getTitle(context)) },
                    supportingContent = {
                        Text(
                            text = command.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    },
                    modifier = Modifier.clickable { nav.pop(command.id) },
                    leadingContent = {
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = tint,
                        )
                    },
                    trailingContent = {
                        ItemMenu { onDismiss, onConfirm ->
                            ItemMenuEditOption(
                                type = command.type,
                                id = command.id,
                                nav = nav,
                                onDismiss = onDismiss,
                            )
                            ItemMenuRunOption(
                                onDismiss = onDismiss,
                                onClick = { command.execute(context, scope) },
                            )
                            ItemMenuDeleteOption(
                                confirmationMessage = stringResource(R.string.commandlist_delete_title),
                                onDismiss = onDismiss,
                                onConfirm = onConfirm,
                                onDelete = { repository.command.remove(command.id) },
                            )
                        }
                    },
                )
            }
        }

        ListFab(
            onClick = { nav.push(CommandTypePicker) },
            icon = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.commandlist_create_fab_desc),
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
