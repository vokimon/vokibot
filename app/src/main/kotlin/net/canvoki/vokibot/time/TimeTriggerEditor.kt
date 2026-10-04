package net.canvoki.vokibot.time

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data class TimeTriggerEditor(
    val triggerId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        // Filled by the Shell step
    }
}
