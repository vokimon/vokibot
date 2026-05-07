package net.canvoki.vokibot

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlin.reflect.KClass
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
    val entityClass: KClass<out StorableEntity>,
    @field:StringRes val labelRes: Int,
    @field:DrawableRes val iconRes: Int,
    val editorFactory: (entityId: String?) -> StackedScreen<Unit>,
)

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
