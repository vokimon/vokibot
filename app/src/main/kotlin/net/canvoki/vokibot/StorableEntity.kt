package net.canvoki.vokibot

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String

    fun toJson(): String

    companion object {
        val registry = EntityRegistry()
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
