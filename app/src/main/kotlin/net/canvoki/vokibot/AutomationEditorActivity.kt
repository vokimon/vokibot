package net.canvoki.vokibot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import net.canvoki.shared.component.AppScaffold

class AutomationEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScaffold {
                // Placeholder: wire your actual AutomationEditor composable here
                // Read extras: intent.getStringExtra("trigger_type"), intent.getStringExtra("trigger_id")
            }
        }
    }
}
