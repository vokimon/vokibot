package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.Serializable

@Serializable
abstract class Command : StorableEntity {
    /**
     * Execute this command.
     */
    abstract suspend fun execute(context: Context)

    companion object {
        fun getRegisteredTypes(): List<EntityTypeInfo> = StorableEntity.registry.getRegisteredTypes(Command::class)

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
    val type: String,
) : Command() {
    override val id: String = "unknown_${type}_${json.hashCode()}"
    override val title: String = "Unsupported Command"
    override val description: String = "`$type` not supported"
    override val iconRes: Int = android.R.drawable.ic_menu_help

    override fun toJson(): String = json

    override suspend fun execute(context: Context) { /* no-op */ }
}
