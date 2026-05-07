package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

fun assertRegisteredTypes(expected: String, actual: List<EntityTypeInfo>) {
    val actualString = actual.map { it.typeKey }.sorted().joinToString("\n")
    assertEquals(expected, actualString)
}

fun typeInfoShortcut() = EntityTypeInfo(
    typeKey = ShortcutTrigger.TYPE,
    labelRes = R.string.triggerlist_option_shortcut,
    iconRes = R.drawable.ic_shortcut,
    editorFactory = { triggerId -> ShortcutTriggerEditor(triggerId) }
)

class EntityRegistryTest {
    @Test
    fun `empty registry returns empty`() {
        val registry = EntityRegistry()
        assertRegisteredTypes("", registry.getRegisteredTypes())
    }

    @Test
    fun `registry with one item`() {
        val registry = EntityRegistry()
        val typeInfo = typeInfoShortcut()
        registry.register(typeInfo)
        assertRegisteredTypes("trigger_shortcut", registry.getRegisteredTypes())
    }
}
