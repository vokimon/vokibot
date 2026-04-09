package net.canvoki.vokibot.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier


@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = remember(isDark) {
        if (isDark) darkColorScheme() else lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            topBar = topBar,
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                content()
            }
        }
    }
}
