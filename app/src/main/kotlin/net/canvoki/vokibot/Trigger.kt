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
    abstract val title: String
    abstract val description: String

    @get:DrawableRes
    abstract val iconRes: Int
    abstract val type: String

    companion object {
        private val typeInfos = mutableMapOf<String, EntityTypeInfo>()

        private val json =
            Json {
                ignoreUnknownKeys = true
                classDiscriminator = "type"
            }

        private fun ensureInitialized() = EntityBootstrap.ensure()

        /**
         * Registers a trigger type with its metadata and JSON factory.
         * The type info must be pre-built and stored in the trigger's companion object.
         */
        fun register(typeInfo: EntityTypeInfo) {
            typeInfos[typeInfo.typeKey] = typeInfo
            StorableEntity.registry.register(typeInfo)
        }

        /** Get all registered trigger types for UI listing */
        fun getRegisteredTypes(): List<EntityTypeInfo> {
            ensureInitialized()
            return StorableEntity.registry.getRegisteredTypes(Trigger::class)
        }

        /** Get the editor screen for a given trigger type and optional id */
        fun getEditorScreen(
            typeKey: String,
            triggerId: String?,
        ): StackedScreen<Unit>? {
            ensureInitialized()
            return StorableEntity.registry.getEditorScreen(typeKey, triggerId)
        }

        /** Deserialize any registered Trigger from JSON */
        fun fromJson(jsonString: String): Trigger {
            ensureInitialized()

            StorableEntity.registry.fromJson(jsonString, Trigger::class)?.let { return it }

            // Error loading returning an unknown trigger
            val type = StorableEntity.registry.extractType(jsonString) ?: "unknown"
            return UnknownTrigger(type = type, json = jsonString)
        }

        /**
         * Composable helper to get a localized, human-readable label for a trigger type.
         * Returns the raw typeKey if the type is not registered (e.g. orphaned data).
         *
         * Usage: Text(Trigger.typeLabel(trigger.type))
         */
        @Composable
        fun typeLabel(type: String): String {
            ensureInitialized()
            return typeInfos[type]?.let {
                stringResource(it.labelRes)
            } ?: type
        }
    }

    @Serializable
    data class PreviewTrigger(
        val type: String,
    )
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
