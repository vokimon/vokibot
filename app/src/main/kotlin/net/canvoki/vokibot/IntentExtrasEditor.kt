package net.canvoki.vokibot

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun launchTestIntent(
    context: Context,
    packageName: String,
    activityName: String,
    extras: List<IntentExtraItem>,
    baseIntent: Intent
) {
    val intent = Intent(baseIntent).apply {
        setClassName(packageName, activityName)

        extras.forEach { extra ->
            putExtra(extra.key, extra.value)
        }
    }

    context.startActivity(intent)
}

// ---- Model (placeholder per futur editor) ----

data class IntentExtraItem(
    val key: String,
    val value: String
)

// ---- UI ----

@Composable
fun IntentExtrasEditor(
    packageName: String,
    activityName: String,
    extras: List<IntentExtraItem> = emptyList(),
    baseIntent: Intent = Intent()
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(
            text = "Extras",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (extras.isEmpty()) {
            Text(
                text = "No extras defined",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            extras.forEach { extra ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = extra.key,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = extra.value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                launchTestIntent(
                    context = context,
                    packageName = packageName,
                    activityName = activityName,
                    extras = extras,
                    baseIntent = baseIntent
                )
                }
        ) {
            Text("Test launch")
        }
    }
}
