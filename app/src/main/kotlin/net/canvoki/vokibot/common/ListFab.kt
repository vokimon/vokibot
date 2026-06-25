package net.canvoki.vokibot.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ListFab(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(16.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
        )
    }
}
