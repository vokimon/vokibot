package net.canvoki.vokibot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.canvoki.shared.component.StackedScreen
import kotlin.reflect.KClass

/**
 * Abstract base for all automation triggers.
 */
@Serializable
abstract class Trigger : StorableEntity {
    companion object {
        /** Get all registered trigger types for UI listing */
        fun getRegisteredTypes(): List<EntityMetadata> = StorableEntity.registry.getRegisteredTypes(Trigger::class)

        /** Deserialize any registered Trigger from JSON */
        fun fromJson(jsonString: String): Trigger {
            StorableEntity.fromJson(jsonString, Trigger::class)?.let { return it }

            // Error loading returning an unknown trigger
            val type = StorableEntity.extractType(jsonString)
            return UnknownTrigger(type = type, json = jsonString)
        }
    }
}

/**
 * Special trigger type for proving trigger types and report missing ones.
 */
@Serializable
data class UnknownTrigger(
    val json: String,
    override val type: String,
) : Trigger() {
    override val id: String = "unknown_${type}_${json.hashCode()}"

    override val title: String = "Unsupported Trigger"

    override val description: String = "`$type` not supported"

    override val iconRes: Int = android.R.drawable.ic_menu_help

    override fun toJson(): String = json
}
