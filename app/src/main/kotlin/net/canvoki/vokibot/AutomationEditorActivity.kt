package net.canvoki.vokibot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import net.canvoki.shared.component.AppScaffold

class AutomationEditorActivity : ComponentActivity() {
    private var createdAutomationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val triggerType = intent.getStringExtra("trigger_type")
        val triggerId = intent.getStringExtra("trigger_id")
        val preselectedTrigger =
            if (triggerType != null && triggerId != null) {
                triggerType to triggerId
            } else {
                null
            }

        if (savedInstanceState == null && preselectedTrigger != null) {
            val repository = FileDataRepository.fromContext(this)
            val automation =
                Automation(
                    name = "On $triggerType $triggerId",
                    triggerType = preselectedTrigger.first,
                    triggerId = preselectedTrigger.second,
                    commandIds = emptyList(),
                )
            repository.automation.save(automation)
            createdAutomationId = automation.id
        }

        setContent {
            AppScaffold {
                IntentActionBuilder(
                    initialStack =
                        listOf(
                            BuilderScreen.AutomationList,
                            BuilderScreen.AutomationEditor(editingId = createdAutomationId),
                        ),
                )
            }
        }
    }
}
