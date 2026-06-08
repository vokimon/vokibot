package net.canvoki.vokibot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.preferences.PreferenceCategory
import net.canvoki.shared.settings.LanguageSettings
import net.canvoki.shared.settings.ThemeSettings
import net.canvoki.vokibot.R

@Composable
fun Drawer() {
    Column(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        PreferenceCategory("Settings") {
            LanguageSettings.Preference()
            ThemeSettings.Preference()
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
