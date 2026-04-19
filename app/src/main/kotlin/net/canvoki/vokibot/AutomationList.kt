package net.canvoki.vokibot

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.AsyncList

@Composable
fun AutomationList(
    onNewAutomation: () -> Unit,
    onAutomationSelected: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    var automationToDelete by remember { mutableStateOf<Automation?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncList(
            refreshKeys = listOf(refreshCounter),
            loader = { repository.automation.all() },
            itemKey = { it.id },
            groupBy = { "automation" },
            headerContent = { key -> AutomationGroupHeader(key) },
            notFoundMessage = stringResource(R.string.automationlist_not_found),
        ) { automation ->
            var menuExpanded by remember { mutableStateOf(false) }

            val triggerDisplayName =
                remember(automation.triggerType, automation.triggerId) {
                    if (automation.triggerType ==
                        "nfc"
                    ) {
                        repository.nfcTrigger.load(automation.triggerId)?.displayName
                    } else {
                        null
                    }
                }

            ListItem(
                headlineContent = { Text(automation.name) },
                supportingContent = {
                    Text(
                        text =
                            buildString {
                                triggerDisplayName?.let { append("$it • ") }
                                append("${automation.commandIds.size} command(s)")
                            },
                        maxLines = 1,
                    )
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_check_circle),
                        contentDescription = stringResource(R.string.automation_type_automation),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.clickable { onAutomationSelected(automation.id) },
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
            onClick = { onNewAutomation() },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.automationlist_create_fab_desc),
            )
        }
    }

    if (automationToDelete != null) {
        AlertDialog(
            onDismissRequest = { automationToDelete = null },
            title = { Text(stringResource(R.string.automationlist_delete_title)) },
            text = { Text(stringResource(R.string.automationlist_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        automationToDelete?.let { auto ->
                            repository.automation.remove(auto.id)
                            refreshCounter++
                            automationToDelete = null
                        }
                    },
                ) {
                    Text(stringResource(R.string.automationlist_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { automationToDelete = null }) {
                    Text(stringResource(R.string.automationlist_cancel))
                }
            },
        )
    }
}

@Composable
private fun AutomationGroupHeader(groupKey: String) {
    Text(
        text = stringResource(R.string.automation_group_automation),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
