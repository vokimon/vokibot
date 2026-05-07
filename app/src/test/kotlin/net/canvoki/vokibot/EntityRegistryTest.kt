package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

fun assertRegisteredTypes(expected: String, actual: List<EntityTypeInfo>) {
    val actualString = actual.map { it.typeKey }.sorted().joinToString("\n")
    assertEquals(expected, actualString)
}

fun typeInfoShortcut() = EntityTypeInfo(
    typeKey = ShortcutTrigger.TYPE,
    entityClass = ShortcutTrigger::class,
    labelRes = R.string.triggerlist_option_shortcut,
    iconRes = R.drawable.ic_shortcut,
    editorFactory = { triggerId -> ShortcutTriggerEditor(triggerId) }
)

fun typeInfoNfc() = EntityTypeInfo(
    typeKey = NfcTrigger.TYPE,
    entityClass = NfcTrigger::class,
    labelRes = R.string.triggerlist_option_nfc,
    iconRes = R.drawable.ic_nfc,
    editorFactory = { triggerId -> NfcTriggerEditor(triggerId) }
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
        registry.register(typeInfoShortcut())
        assertRegisteredTypes("trigger_shortcut", registry.getRegisteredTypes())
    }

    @Test
    fun `registry with many items`() {
        val registry = EntityRegistry()
        registry.register(typeInfoShortcut())
        registry.register(typeInfoNfc())
        assertRegisteredTypes("trigger_nfc\ntrigger_shortcut", registry.getRegisteredTypes())
    }

    @Test
    fun `registry filters by same type`() {
        val registry = EntityRegistry()
        registry.register(typeInfoShortcut())
        registry.register(typeInfoNfc())
        assertRegisteredTypes("trigger_nfc", registry.getRegisteredTypes(NfcTrigger::class))
    }
}
