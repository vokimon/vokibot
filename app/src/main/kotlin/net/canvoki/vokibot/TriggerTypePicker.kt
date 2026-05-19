package net.canvoki.vokibot

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data object TriggerTypePicker : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        EntityTypePicker(
            types = Trigger.getRegisteredTypes(),
            nav = nav,
            title = stringResource(R.string.entity_picker_trigger_type),
            modifier = Modifier.fillMaxSize(),
        )
    }
}
