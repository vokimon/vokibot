package net.canvoki.vokibot

import androidx.compose.runtime.Composable
import net.canvoki.shared.component.StackNavigatorState
import net.canvoki.shared.component.StackedScreen
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test

object DummyScreen : StackedScreen<Unit>() {
    @Composable
    override fun Screen(nav: StackNavigatorState) {}
}

fun assertRegisteredTypes(
    expected: String,
    actual: List<EntityMetadata>,
) {
    val actualString = actual.map { it.typeKey }.sorted().joinToString("\n")
    assertEquals(expected, actualString)
}

fun typeInfoShortcut() = ShortcutTrigger.TYPE_INFO

fun typeInfoNfc() = NfcTrigger.TYPE_INFO

fun typeInfoAutomation(typeKey: String = "automation") =
    EntityTypeInfo(
        typeKey = typeKey,
        entityClass = Automation::class,
        labelRes = 0,
        iconRes = 0,
        editorFactory = { DummyScreen },
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

    @Test
    fun `registry filters by super class`() {
        val registry = EntityRegistry()
        registry.register(typeInfoShortcut())
        registry.register(typeInfoNfc())
        registry.register(typeInfoAutomation())
        // Should include both Trigger subclasses, exclude Automation
        assertRegisteredTypes("trigger_nfc\ntrigger_shortcut", registry.getRegisteredTypes(Trigger::class))
    }

    @Test
    fun `fromJson same type returns the object`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())
        registry.register(typeInfoShortcut())

        val json = """{"type":"trigger_nfc","displayName":"Test NFC","uid":"04:AB:12:CD:56:78:90"}"""

        val result = registry.fromJson(json, NfcTrigger::class)

        assertDataEqual(NfcTrigger.fromJson(json), result)
    }

    @Test
    fun `fromJson supertype returns the object`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())
        registry.register(typeInfoShortcut())

        val json = """{"type":"trigger_nfc","displayName":"Test NFC","uid":"04:AB:12:CD:56:78:90"}"""

        val result = registry.fromJson(json, Trigger::class)

        assertDataEqual(NfcTrigger.fromJson(json), result)
    }

    @Test
    fun `fromJson bad type returns null`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())
        registry.register(typeInfoShortcut())

        val json = """{"type":"trigger_nfc","displayName":"Test NFC","uid":"04:AB:12:CD:56:78:90"}"""

        val result = registry.fromJson(json, ShortcutTrigger::class)

        assertDataEqual(null, result)
    }

    @Test
    fun `fromJson non registered returns null`() {
        val registry = EntityRegistry()
        //registry.register(typeInfoNfc()) // Not registered
        registry.register(typeInfoShortcut())

        val json = """{"type":"trigger_nfc","displayName":"Test NFC","uid":"04:AB:12:CD:56:78:90"}"""

        val result = registry.fromJson(json, NfcTrigger::class)

        assertDataEqual(null, result)
    }

    @Test
    fun `fromJson non object returns null`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())

        val json = """[{"type":"trigger_nfc"}]"""

        val result = registry.fromJson(json, NfcTrigger::class)

        assertDataEqual(null, result)
    }

    @Test
    fun `fromJson missing type returns null`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())

        val json = """{"displayName":"Test NFC"}"""

        val result = registry.fromJson(json, NfcTrigger::class)

        assertDataEqual(null, result)
    }

    @Test
    fun `fromJson type not string returns null`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())

        val json = """{"type":123}"""

        val result = registry.fromJson(json, NfcTrigger::class)

        assertDataEqual(null, result)
    }

    @Test
    fun `getEditorScreen with null id`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())

        val result = registry.getEditorScreen("trigger_nfc", null)

        assertEquals(NfcTriggerEditor(editingId = null), result)
    }

    @Test
    fun `getEditorScreen with an id`() {
        val registry = EntityRegistry()
        registry.register(typeInfoNfc())
        val testId = "test_trigger_123"

        val result = registry.getEditorScreen("trigger_nfc", testId)

        assertEquals(NfcTriggerEditor(editingId = testId), result)
    }

    @Test
    fun `getEditorScreen with a different type`() {
        val registry = EntityRegistry()
        registry.register(typeInfoShortcut())
        registry.register(typeInfoNfc())

        val result = registry.getEditorScreen("trigger_shortcut", null)

        assertEquals(ShortcutTriggerEditor(), result)
    }

    @Test
    fun `getEditorScreen returns null for unregistered type`() {
        val registry = EntityRegistry()
        registry.register(typeInfoShortcut()) // Only register shortcut

        val result = registry.getEditorScreen("trigger_nfc", null) // Ask for NFC

        assertEquals(null, result)
    }
}
