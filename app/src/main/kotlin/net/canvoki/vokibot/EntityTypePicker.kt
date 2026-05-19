package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.ContextualHelpButton
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.vokibot.common.EditorHeader

@Composable
fun EntityTypePicker(
    types: List<EntityMetadata>,
    nav: StackNavigatorState,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EditorHeader(
            icon = painterResource(android.R.drawable.ic_menu_add),
            title = title,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            items(types) { type ->
                val label = stringResource(type.labelRes)
                val helpText = stringResource(type.helpRes)
                val firstParagraph = helpText.split("\n\n").firstOrNull()?.replace("\n", " ") ?: helpText

                ListItem(
                    headlineContent = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    supportingContent = {
                        Text(
                            text = firstParagraph,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(type.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        ContextualHelpButton(
                            title = label,
                            description = helpText,
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                StorableEntity
                                    .getEditorScreen(type.typeKey, null)
                                    ?.let {
                                        nav.push(it) {
                                            nav.pop()
                                        }
                                    }
                            },
                )
                HorizontalDivider()
            }
        }
    }
}
