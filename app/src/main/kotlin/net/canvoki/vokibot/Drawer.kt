package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import net.canvoki.shared.component.preferences.PreferenceCategory
import net.canvoki.shared.settings.LanguageSettings
import net.canvoki.shared.settings.ThemeSettings
import net.canvoki.vokibot.R

@Composable
fun Drawer() {
    PreferenceCategory("Settings") {
        LanguageSettings.Preference()
        ThemeSettings.Preference()
    }
}

