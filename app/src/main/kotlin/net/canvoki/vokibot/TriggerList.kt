package net.canvoki.vokibot

import android.graphics.drawable.VectorDrawable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.BadgeDrawable
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.ListGroupHeader
import net.canvoki.vokibot.drawableToPainter

@Serializable
data object TriggerList : StackedScreen<String>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        TriggerList(nav)
    }
}

@Composable
fun TriggerList(
    nav: StackNavigatorState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    var triggerToDelete by remember { mutableStateOf<Trigger?>(null) }
    val dataVersion = repository.rememberDataVersion()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                icon = painterResource(Trigger.iconRes),
                title = stringResource(R.string.triggerlist_header),
            )
            AsyncList(
                refreshKeys = listOf(dataVersion),
                loader = { repository.trigger.all() },
                itemKey = { it.id },
                groupBy = { it.type },
                headerContent = { key ->
                    val helpResId = StorableEntity.helpResId(key)
                    ListGroupHeader(
                        title = StorableEntity.label(key),
                        helpDescription = helpResId.takeIf { it != 0 }?.let { stringResource(it) },
                    )
                },
                notFoundMessage = stringResource(R.string.triggerlist_not_found),
            ) { trigger ->
                var menuExpanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text(trigger.getTitle(context)) },
                    supportingContent = { Text(trigger.description) },
                    leadingContent = {
                        val triggerIcon = remember(trigger.id) { trigger.loadIcon(context) }
                        Icon(
                            painter = drawableToPainter(triggerIcon),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint =
                                when (triggerIcon) {
                                    is VectorDrawable, is BadgeDrawable -> MaterialTheme.colorScheme.primary
                                    else -> Color.Unspecified
                                },
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier =
                        Modifier.clickable {
                            nav.pop(trigger.id)
                        },
                    trailingContent = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.triggerlist_options_desc),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.triggerlist_edit)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    val editorScreen = StorableEntity.getEditorScreen(trigger.type, trigger.id)
                                    editorScreen?.let {
                                        nav.push(it)
                                    }
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.triggerlist_delete)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    triggerToDelete = trigger
                                },
                            )
                        }
                    },
                )
            }
        }

        FloatingActionButton(
            onClick = { nav.push(TriggerTypePicker) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.triggerlist_create_fab_desc),
            )
        }
    }

    // Delete confirmation dialog
    ConfirmDialog(
        show = triggerToDelete != null,
        title = stringResource(R.string.triggerlist_delete_title),
        text = stringResource(R.string.triggerlist_delete_message),
        confirmText = stringResource(R.string.triggerlist_delete),
        dismissText = stringResource(R.string.triggerlist_cancel),
        onDismiss = {
            triggerToDelete = null
        },
        onConfirm = {
            triggerToDelete?.let { t ->
                repository.trigger.remove(t.id)
                triggerToDelete = null
            }
        },
    )
}
