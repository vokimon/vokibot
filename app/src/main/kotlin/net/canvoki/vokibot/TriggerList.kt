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
import net.canvoki.shared.component.ChooserDialog
import net.canvoki.shared.component.ChooserOption


@Composable
fun TriggerList(nav: ScreenNavigator) {
    TriggerList(
        onNewTrigger = { typeTag ->
            if (typeTag == "nfc") {
                nav.push(BuilderScreen.NfcTriggerEditor) { result: Unit? -> }
            }
        },
        onTriggerSelected = { type, id ->
            nav.back(Pair(type, id))
        },
    )
}

@Composable
fun TriggerList(
    onNewTrigger: (String) -> Unit,
    onTriggerSelected: (type: String, id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { FileDataRepository.fromContext(context) }
    var showTypeChooser by remember { mutableStateOf(false) }
    var refreshCounter by remember { mutableIntStateOf(0) }
    var triggerToDelete by remember { mutableStateOf<NfcTrigger?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        AsyncList(
            refreshKeys = listOf(refreshCounter),
            loader = { repository.nfcTrigger.all() },
            itemKey = { it.id },
            groupBy = { "nfc" },
            headerContent = { key -> TriggerGroupHeader(key) },
            notFoundMessage = stringResource(R.string.triggerlist_not_found),
        ) { trigger ->
            var menuExpanded by remember { mutableStateOf(false) }

            ListItem(
                headlineContent = { Text(trigger.displayName) },
                supportingContent = { Text(trigger.uid) },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_nfc),
                        contentDescription = null, //stringResource(R.string.trigger_type_nfc),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.clickable { onTriggerSelected("nfc", trigger.id) },
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

        FloatingActionButton(
            onClick = { showTypeChooser = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.triggerlist_create_fab_desc),
            )
        }
    }

    // Delete confirmation dialog
    if (triggerToDelete != null) {
        AlertDialog(
            onDismissRequest = { triggerToDelete = null },
            title = { Text(stringResource(R.string.triggerlist_delete_title)) },
            text = { Text(stringResource(R.string.triggerlist_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerToDelete?.let { t ->
                            repository.nfcTrigger.remove(t.id)
                            refreshCounter++
                            triggerToDelete = null
                        }
                    },
                ) {
                    Text(stringResource(R.string.triggerlist_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { triggerToDelete = null }) {
                    Text(stringResource(R.string.triggerlist_cancel))
                }
            },
        )
    }

    if (showTypeChooser) {
        ChooserDialog(
            title = stringResource(R.string.triggerlist_create_title),
            options =
                listOf(
                    ChooserOption(value = "nfc", label = stringResource(R.string.triggerlist_option_nfc)),
                    ChooserOption(value = "nfc", label = stringResource(R.string.triggerlist_option_nfc)),
                ),
            selectedValue = "",
            onConfirm = { value ->
                showTypeChooser = false
                onNewTrigger(value)
            },
            onDismiss = {
                showTypeChooser = false
            },
        )
    }
}

@Composable
private fun TriggerGroupHeader(groupKey: String) {
    Text(
        text = stringResource(R.string.trigger_group_nfc),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
