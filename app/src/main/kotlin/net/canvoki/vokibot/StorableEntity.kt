package net.canvoki.vokibot

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String

    fun toJson(): String
}
