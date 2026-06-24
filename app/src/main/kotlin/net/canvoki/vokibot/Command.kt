package net.canvoki.vokibot

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.canvoki.shared.usermessage.UserMessage

/**
 * Abstract class for all the actions you can perform as response to a trigger
 */
@Serializable
abstract class Command : StorableEntity {
    /**
     * Execute this command.
     */
    abstract suspend fun execute(context: Context)

    /**
     * Execute this command, showing a toast on error.
     */
    fun execute(
        context: Context,
        scope: CoroutineScope,
    ) {
        scope.launch {
            try {
                execute(context)
            } catch (e: Exception) {
                e.printStackTrace()
                UserMessage.Info(e.message ?: context.getString(R.string.command_run_error_fallback)).post()
            }
        }
    }

    companion object {
        val iconRes = R.drawable.ic_task_alt

        fun getRegisteredTypes(): List<EntityMetadata> = StorableEntity.registry.getRegisteredTypes(Command::class)

        fun fromJson(jsonString: String): Command {
            StorableEntity.fromJson(jsonString, Command::class)?.let { return it }
            val type = StorableEntity.extractType(jsonString)
            return UnknownCommand(type = type, json = jsonString)
        }
    }
}

@Serializable
data class UnknownCommand(
    val json: String,
    override val type: String,
) : Command() {
    override val id: String = "unknown_${type}_${json.hashCode()}"

    override fun getTitle(context: Context): String = context.getString(R.string.unknown_command_title)

    override val description: String = type
    override val iconRes: Int = android.R.drawable.ic_menu_help

    override fun toJson(): String = json

    override suspend fun execute(context: Context) { /* no-op */ }
}
