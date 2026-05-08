package net.canvoki.vokibot

import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.vokibot.common.EditorHeader

@Serializable
data class SettingsPageCommandEditor(
    val editingId: String? = null,
) : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {
        val context = LocalContext.current
        val repository = remember { FileDataRepository.fromContext(context) }
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorHeader(
                title = stringResource(R.string.settings_page_header),
                icon = painterResource(R.drawable.ic_settings),
                actionText = stringResource(R.string.settings_page_done),
                action = {
                    val command =
                        SettingsPageCommand(
                            pageId = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
                        )
                    repository.command.save(command)
                    nav.pop()
                },
                actionEnabled = true,
            )

            Text(
                text = "TODO: Add settings selection UI",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
