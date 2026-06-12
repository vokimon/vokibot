package net.canvoki.vokibot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AutomationTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    private fun automationBase(
        name: String = "Test",
        triggerId: String = "abc",
        commandIds: List<String> = emptyList(),
        id: String? = null,
    ) = Automation(
        name = name,
        triggerId = triggerId,
        commandIds = commandIds,
        id = id,
    )

    @Test
    fun `secondary constructor generates UUID when no id provided`() {
        val automation = automationBase()

        assertTrue(automation.id.isNotBlank())
    }

    @Test
    fun `secondary constructor generates UUID when id is explicitly null`() {
        val automation = automationBase(id = null)

        assertTrue(automation.id.isNotBlank())
    }

    @Test
    fun `secondary constructor generates different UUIDs for separate instances`() {
        val a = automationBase()
        val b = automationBase()

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `secondary constructor uses provided id when not null`() {
        val automation = automationBase(id = "my-fixed-id")

        assertEquals("my-fixed-id", automation.id)
    }

    @Test
    fun `serialization preserves generated id`() {
        val original = automationBase()
        val restored = Automation.fromJson(original.toJson())

        assertEquals(original.id, restored.id)
    }

    @Test
    fun `save and load roundtrip preserves auto-generated id`() {
        val dataset = DataSet(tempFolder.root, "automation_", Automation::fromJson)
        val automation = automationBase()

        dataset.save(automation)
        val loaded = dataset.load(automation.id)

        assertEquals(automation.id, loaded?.id)
        assertEquals(automation.name, loaded?.name)
    }

    @Test
    fun `save and load roundtrip preserves explicit id`() {
        val dataset = DataSet(tempFolder.root, "automation_", Automation::fromJson)
        val automation = automationBase(id = "explicit-id")

        dataset.save(automation)
        val loaded = dataset.load("explicit-id")

        assertEquals("explicit-id", loaded?.id)
        assertEquals(automation.name, loaded?.name)
    }

    @Test
    fun `editing existing automation keeps same id after rename`() {
        val dataset = DataSet(tempFolder.root, "automation_", Automation::fromJson)
        val original = automationBase(name = "Old Name", id = "original-id")
        dataset.save(original)

        val edited =
            Automation(
                id = original.id,
                name = "New Name",
                triggerId = original.triggerId,
                commandIds = original.commandIds,
            )
        dataset.save(edited)

        val loaded = dataset.load("original-id")
        assertEquals("original-id", loaded?.id)
        assertEquals("New Name", loaded?.name)
    }
}
