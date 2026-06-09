package net.canvoki.vokibot

import android.content.Context
import androidx.annotation.DrawableRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.canvoki.shared.log
import java.util.UUID

@Serializable
data class Automation(
    override val id: String,
    val name: String,
    val triggerType: String,
    val triggerId: String,
    val commandIds: List<String>,
) : StorableEntity {
    override val type: String = "automation"
    constructor(
        name: String,
        triggerType: String,
        triggerId: String,
        commandIds: List<String>,
        id: String? = null,
    ) : this(id ?: UUID.randomUUID().toString(), name, triggerType, triggerId, commandIds)

    override fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    override fun getTitle(context: Context): String = name

    override val description: String
        get() = "$triggerType • ${commandIds.size} command(s)"

    @get:DrawableRes
    override val iconRes: Int get() = Companion.iconRes

    companion object : EntityMetadata {
        override val typeKey = "automation"
        override val entityClass = Automation::class
        override val labelRes = R.string.automation_type_automation
        override val iconRes = R.drawable.ic_smart_toy
        override val editorFactory = { id: String? -> AutomationEditor(id) }
        override val deserializer = { jsonString: String -> fromJson(jsonString) }
        override val helpRes = R.string.automation_help

        fun fromJson(jsonString: String): Automation = JsonConfig.decodeFromString(serializer(), jsonString)

        fun register() = StorableEntity.register(this)

        fun executeByTrigger(
            repo: FileDataRepository,
            triggerId: String,
            context: Context,
            onExecuted: (() -> Unit)? = null,
        ): Boolean {
            val automations = repo.automation.all().filter { it.triggerId == triggerId }
            if (automations.isEmpty()) return false
            log("Automation.executeByTrigger: Dispatching ${automations.size} automation(s) for $triggerId")
            CoroutineScope(Dispatchers.IO).launch {
                automations.forEach { automation ->
                    automation.commandIds.forEach { cmdId ->
                        repo.command.load(cmdId)?.execute(context)
                    }
                }
                onExecuted?.invoke()
            }
            return true
        }
    }
}
