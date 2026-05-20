package net.canvoki.vokibot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
data object ActivityLaunchCommandEditor : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                icon = painterResource(LaunchActivityCommand.iconRes),
                title = stringResource(LaunchActivityCommand.labelRes),
                actionText = stringResource(R.string.automation_done),
                action = { nav.pop() },
            )
            Text("ActivityLaunchCommandEditor - TBD")
        }
    }
}
