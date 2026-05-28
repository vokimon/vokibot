package net.canvoki.vokibot

import android.content.Context
import android.graphics.drawable.Drawable
import kotlinx.serialization.Serializable

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
     * Load an icon representing this command.
     * Default uses iconRes as fallback.
     */
    open fun loadIcon(context: Context): Drawable = context.getDrawable(iconRes)!!

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
