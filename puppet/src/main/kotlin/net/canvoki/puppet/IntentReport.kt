package net.canvoki.puppet

import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.canvoki.shared.component.AppScaffold

@Composable
fun IntentReport(activityName: String, intent: Intent) {
    AppScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Intent Received", style = MaterialTheme.typography.headlineMedium)
            Text("Responding Actiity: ${activityName}")
            Text("Action: ${intent.action ?: "null"}")
            Text("Package: ${intent.`package` ?: "null"}")
            Text("Component: ${intent.component?.flattenToShortString() ?: "null"}")
            Text("Data: ${intent.data?.toString() ?: "null"}")
            Text("Type: ${intent.type ?: "null"}")
            Text("Flags: 0x${intent.flags.toString(16).uppercase()}")

            Text("\nExtras:", style = MaterialTheme.typography.titleMedium)
            val extras = intent.extras
            if (extras != null && !extras.isEmpty) {
                for (key in extras.keySet()) {
                    val value = extras[key]?.toString() ?: "null"
                    val type = extras[key]?.javaClass?.simpleName
                    Text("  $key (${type}) = $value")
                }
            } else {
                Text("  (none)")
            }
        }
    }
}
