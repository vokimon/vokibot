package net.canvoki.vokibot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.AsyncList
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader
import net.canvoki.vokibot.common.ListGroupHeader

@Serializable
data object SettingList : StackedScreen<String>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        SettingList(nav)
    }
}

@Composable
fun SettingList(nav: StackNavigatorState) {
    Column(modifier = Modifier.fillMaxSize()) {
        EditorHeader(
            icon = painterResource(ChangeSettingCommand.iconRes),
            title = stringResource(ChangeSettingCommand.labelRes),
        )
        AsyncList(
            loader = { SettingSpec.all() },
            itemKey = { it.id },
            groupBy = { it.category.name },
            headerContent = { key ->
                val category = SettingCategory.valueOf(key)
                ListGroupHeader(title = stringResource(category.labelRes))
            },
            notFoundMessage = stringResource(R.string.setting_list_not_found),
        ) { setting ->
            ListItem(
                headlineContent = { Text(stringResource(setting.name)) },
                supportingContent = { Text(stringResource(setting.description)) },
                trailingContent = {
                    Text(
                        text = setting.namespace.name,
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            when (setting.namespace) {
                                SettingNamespace.SYSTEM -> MaterialTheme.colorScheme.primary
                                SettingNamespace.SECURE -> MaterialTheme.colorScheme.error
                                SettingNamespace.GLOBAL -> MaterialTheme.colorScheme.tertiary
                            },
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { nav.pop(setting.id) },
            )
        }
    }
}
