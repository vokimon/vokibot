package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import net.canvoki.vokibot.common.ItemMenu
import net.canvoki.vokibot.common.ItemMenuDeleteOption
import net.canvoki.vokibot.common.ListFab
import net.canvoki.vokibot.common.ListGroupHeader

@Serializable
data object AutomationList : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }
        val dataVersion = repository.rememberDataVersion()

        Box(modifier = Modifier.fillMaxSize()) {
            AsyncList(
                refreshKeys = listOf(dataVersion),
                loader = { repository.automation.all() },
                itemKey = { it.id },
                groupBy = { "automation" },
                headerContent = {
                    ListGroupHeader(
                        title = stringResource(R.string.automation_group_automation),
                    )
                },
                notFoundMessage = stringResource(R.string.automationlist_not_found),
            ) { automation ->
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
                        ItemMenu { onDismiss, onConfirm ->
                            ItemMenuDeleteOption(
                                confirmationMessage = stringResource(R.string.automationlist_delete_title),
                                onDismiss = onDismiss,
                                onConfirm = onConfirm,
                                onDelete = { repository.automation.remove(automation.id) },
                            )
                        }
                    },
                )
            }

            ListFab(
                onClick = { nav.push(AutomationEditor(null)) },
                icon = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.automationlist_create_fab_desc),
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }
    }
}
