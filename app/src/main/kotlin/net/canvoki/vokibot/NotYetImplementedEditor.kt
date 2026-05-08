package net.canvoki.vokibot

import androidx.compose.foundation.layout.Box
import kotlinx.serialization.Serializable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen

@Serializable
data object NotYetImplementedEditor : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.not_yet_implemented))
        }
    }
}