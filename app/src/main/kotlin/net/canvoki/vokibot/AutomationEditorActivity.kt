package net.canvoki.vokibot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.StackNavigator

class AutomationEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val triggerId = intent.getStringExtra("trigger_id")

        setContent {
            AppScaffold {
                StackNavigator(
                    AutomationList,
                    AutomationEditor(prefillTriggerId = triggerId),
                )
            }
        }
    }
}
