package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.preferences.PreferenceCategory
import net.canvoki.shared.settings.LanguageSettings
import net.canvoki.shared.settings.ThemeSettings

@Composable
fun Drawer() {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        PreferenceCategory(stringResource(R.string.dasher_group_settings)) {
            LanguageSettings.Preference()
            ThemeSettings.Preference()
        }
        PreferenceCategory(stringResource(R.string.dasher_group_data_exchange)) {
            ExportOption()
            ImportOption()
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
