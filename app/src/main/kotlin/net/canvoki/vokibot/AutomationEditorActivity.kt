package net.canvoki.vokibot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.canvoki.shared.component.AppScaffold
import net.canvoki.shared.component.StackNavigator

class AutomationEditorActivity : ComponentActivity() {
    private var createdAutomationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = FileDataRepository.fromContext(this)

        val triggerId = intent.getStringExtra("trigger_id")
        val trigger = triggerId?.let { id -> repository.trigger.load(id) }

        if (savedInstanceState == null && trigger != null) {
            val automation =
                Automation(
                    name = "On ${trigger.type} $triggerId", // TODO: translate
                    triggerId = trigger.id,
                    commandIds = emptyList(),
                )
            repository.automation.save(automation)
            createdAutomationId = automation.id
        }

        setContent {
            AppScaffold {
                StackNavigator(
                    AutomationList,
                    AutomationEditor(editingId = createdAutomationId),
                )
            }
        }
    }
}
