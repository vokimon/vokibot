package net.canvoki.vokibot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import net.canvoki.shared.component.ContextualHelpButton
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.component.preferences.rememberMutablePreference
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data class SettingsPageCommandEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }

        val existingCommand =
            remember {
                editingId?.let { repository.command.load(it) as? SettingsPageCommand }
            }

        var selectedPageId by rememberSaveable {
            mutableStateOf(existingCommand?.pageId ?: "")
        }
        var showAll by rememberMutablePreference("settigs_page_editor_show_all", false)
        val scope = rememberCoroutineScope()

        LaunchedEffect(editingId) {
            existingCommand?.let { selectedPageId = it.pageId }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            EditorHeader(
                icon = painterResource(R.drawable.ic_settings),
                title = stringResource(R.string.settings_page_header),
                actionText = stringResource(R.string.settings_page_done),
                action = { nav.pop() },
                actionEnabled = selectedPageId.isNotEmpty(),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Show all pages", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = showAll,
                    onCheckedChange = { showAll = it },
                )
            }

            val displayedPages =
                remember(showAll) {
                    if (showAll) {
                        SETTINGS_PAGES
                    } else {
                        SETTINGS_PAGES.filter { it.isMain }
                    }
                }

            AsyncList(
                refreshKeys = listOf(showAll),
                loader = { displayedPages },
                itemKey = { it.id },
                groupBy = { it.category },
                headerContent = { groupKey -> SettingsPageGroupHeader(groupKey) },
                notFoundMessage = "No pages available",
            ) { page ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPageId = page.id
                                repository.command.save(
                                    SettingsPageCommand(pageId = page.id),
                                )
                                nav.pop()
                            }.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = page.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color =
                            if (page.id == selectedPageId) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                SettingsPageCommand(pageId = page.id).execute(context)
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play_arrow),
                            contentDescription = "Try",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsPageGroupHeader(groupKey: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = groupKey,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
        )
    }
}
