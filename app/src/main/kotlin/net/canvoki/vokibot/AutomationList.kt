package net.canvoki.vokibot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data object AutomationList : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }
        var automationToDelete by remember { mutableStateOf<Automation?>(null) }
        val dataVersion = repository.rememberDataVersion()

        Box(modifier = Modifier.fillMaxSize()) {
            AsyncList(
                refreshKeys = listOf(dataVersion),
                loader = { repository.automation.all() },
                itemKey = { it.id },
                groupBy = { "automation" },
                headerContent = {
                    AutomationGroupHeader(
                        title = stringResource(R.string.automation_group_automation),
                    )
                },
                notFoundMessage = stringResource(R.string.automationlist_not_found),
            ) { automation ->
                var menuExpanded by remember { mutableStateOf(false) }
                var automationDescription =
                    remember(automation.triggerId, automation.commandIds) {
                        buildString {
                            append(repository.trigger.load(automation.triggerId)?.getTitle(context))
                            append(" » ")
                            automation.commandIds.map { id ->
                                append(repository.command.load(id)?.getTitle(context))
                            }
                        }
                    }

                val triggerDisplayName =
                    remember(automation.triggerId) {
                        repository.trigger.load(automation.triggerId)?.getTitle(context)
                    }

                ListItem(
                    headlineContent = {
                        Text(
                            text = automation.getTitle(context),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = automationDescription,
                            maxLines = 2,
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(Automation.iconRes),
                            contentDescription = stringResource(R.string.automation_type_automation),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier =
                        Modifier.clickable {
                            nav.push(AutomationEditor(automation.id))
                        },
                    trailingContent = {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_more_vert),
                                contentDescription = stringResource(R.string.automationlist_options_desc),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.automationlist_delete)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    automationToDelete = automation
                                },
                            )
                        }
                    },
                )
            }

            FloatingActionButton(
                onClick = {
                    nav.push(AutomationEditor(null))
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.automationlist_create_fab_desc),
                )
            }
        }

        ConfirmDialog(
            show = automationToDelete != null,
            title = stringResource(R.string.automationlist_delete_title),
            text = stringResource(R.string.automationlist_delete_message),
            confirmText = stringResource(R.string.automationlist_delete),
            dismissText = stringResource(R.string.automationlist_cancel),
            onConfirm = {
                automationToDelete?.let { auto ->
                    repository.automation.remove(auto.id)
                    automationToDelete = null
                }
            },
            onDismiss = {
                automationToDelete = null
            },
        )
    }
}

@Composable
private fun AutomationGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
