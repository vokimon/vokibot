package net.canvoki.vokibot
import net.canvoki.shared.component.StackedScreen

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String

    fun toJson(): String

    companion object {
        private fun ensureInitialized() = EntityBootstrap.ensure()

        val registry = EntityRegistry()
        fun register(typeinfo: EntityTypeInfo) = registry.register(typeinfo)
        fun getEditorScreen(
            type: String,
            id: String?,
        ): StackedScreen<Unit>? {
            ensureInitialized()
            return registry.getEditorScreen(type, id)
        }
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
    }
}
