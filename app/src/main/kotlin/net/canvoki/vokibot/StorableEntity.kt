package net.canvoki.vokibot

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import kotlinx.serialization.json.Json
import net.canvoki.shared.component.StackedScreen
import kotlin.reflect.KClass

val JsonConfig =
    Json {
        explicitNulls = false
        encodeDefaults = true
        classDiscriminator = "type"
    }

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String

    val type: String

    abstract fun getTitle(context: Context): String

    val description: String

    @get:DrawableRes
    val iconRes: Int

    fun toJson(): String

    companion object {
        private fun ensureInitialized() = EntityBootstrap.ensure()

        private val uninitializedRegistry = EntityRegistry()

        val registry: EntityRegistry
            get() = ensureInitialized().let { uninitializedRegistry }

        /**
         * Registers an entity type with its metadata and JSON factory.
         */
        fun register(metadata: EntityMetadata) = uninitializedRegistry.register(metadata)

        /** Get the editor screen for a given entity type and optional id */
        fun getEditorScreen(
            type: String,
            id: String?,
        ): StackedScreen<Unit>? = registry.getEditorScreen(type, id)

        /**
         * Composable helper to get a localized, human-readable label for a storable type.
         * Returns the raw typeKey if the type is not registered (e.g. orphaned data).
         *
         * Usage: Text(StorableEntity.typeLabel(storable.type))
         */
        @Composable
        fun label(type: String): String = registry.label(type)

        /** Returns the help text resource ID for a type, or 0 if not defined */
        fun helpResId(type: String): Int = registry.helpResId(type)

        /** Get all registered storable types for UI listing */
        fun getRegisteredTypes(baseClass: KClass<out StorableEntity> = StorableEntity::class): List<EntityMetadata> =
            registry.getRegisteredTypes(baseClass)

        /** Deserialize any registered StorableEntity from JSON */
        fun fromJson(jsonString: String): StorableEntity? = registry.fromJson(jsonString, StorableEntity::class)

        /** Deserialize any registered StorableEntity inheriting baseClass from JSON */
        fun <T : StorableEntity> fromJson(
            jsonString: String,
            baseClass: KClass<T>,
        ): T? = registry.fromJson(jsonString, baseClass)

        /** Extract the type attribute of a json object string */
        fun extractType(jsonString: String) = registry.extractType(jsonString) ?: "unknown"
    }
}

object EntityBootstrap {
    // Avoid to be optimized away
    private var touched = false

    fun ensure() {
        touched = true
    }

    init {
        NfcTrigger.register()
        ShortcutTrigger.register()
        BluetoothDeviceTrigger.register()
        LaunchActivityCommand.register()
        SendBroadcastCommand.register()
        StartServiceCommand.register()
        AccessProviderCommand.register()
        SettingsPageCommand.register()
    }
}
