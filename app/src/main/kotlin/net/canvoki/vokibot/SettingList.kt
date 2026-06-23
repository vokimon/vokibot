package net.canvoki.vokibot

import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data object SettingList : StackedScreen<String>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        SettingListScreen(nav)
    }
}

@Composable
fun SettingListScreen(nav: StackNavigatorState) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EditorHeader(
            icon = painterResource(ChangeSettingCommand.iconRes),
            title = stringResource(ChangeSettingCommand.labelRes),
        )

        ListItem(
            headlineContent = { Text("Adaptive Brightness") },
            supportingContent = { Text("SCREEN_BRIGHTNESS_MODE") },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        nav.pop(Settings.System.SCREEN_BRIGHTNESS_MODE)
                    },
        )
    }
}
