package net.canvoki.vokibot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import net.canvoki.shared.component.StackedScreen

/**
 * Base interface for repository entities.
 * Guarantees stable `id` and self-serialization.
 */
interface StorableEntity {
    val id: String

    fun toJson(): String
}

/**
 * Metadata for a registered entity type, used for UI generation.
 */
data class EntityTypeInfo(
    val typeKey: String,
    @field:StringRes val labelRes: Int,
    @field:DrawableRes val iconRes: Int,
    val editorFactory: (entityId: String?) -> StackedScreen<Unit>,
)
