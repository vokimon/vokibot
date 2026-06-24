package net.canvoki.vokibot.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.ContextualHelpButton

@Composable
fun ListGroupHeader(
    title: String,
    helpDescription: String? = null,
) {
    val verticalPadding = 8.dp
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(vertical = verticalPadding),
        )
        if (helpDescription != null) {
            ContextualHelpButton(
                title = title,
                description = helpDescription,
                modifier = Modifier.padding(vertical = verticalPadding),
            )
        }
    }
}
