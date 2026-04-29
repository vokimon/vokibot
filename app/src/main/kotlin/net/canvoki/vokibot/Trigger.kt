package net.canvoki.vokibot

import kotlinx.serialization.Serializable

/**
 * Abstract base for all automation triggers.
 */
@Serializable
abstract class Trigger : StorableEntity {
    abstract val title: String
    abstract val description: String
    abstract val iconRes: Int
}
