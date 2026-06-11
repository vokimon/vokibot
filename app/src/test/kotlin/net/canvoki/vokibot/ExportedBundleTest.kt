package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test

class ExportedBundleTest {
    private fun aCommand() =
        LaunchActivityCommand(
            id = "cmd-1",
            displayName = "Test Command",
            packageName = "com.test",
            className = "com.test.Main",
        )

    private fun commandJson() =
        """{"id": "cmd-1", "displayName": "Test Command", "packageName":"com.test","className":"com.test.Main","extras":{},"flagList":[],"type":"launch_activity"}"""

    private fun anAutomation() =
        Automation(
            id = "auto-1",
            name = "Test Automation",
            triggerType = "trigger_shortcut",
            triggerId = "trg-1",
            commandIds = listOf("cmd-1"),
        )

    private fun automationJson() =
        """{"id": "auto-1", "name": "Test Automation", "triggerType":"trigger_shortcut","triggerId":"trg-1","commandIds":["cmd-1"],"type": "automation"}"""

    private fun anTrigger() =
        ShortcutTrigger(
            id = "trg-1",
            displayName = "Test Trigger",
        )

    private fun buildBundle(vararg entities: StorableEntity) = ExportedBundle(entities = entities.toList())

    private fun buildJson(vararg entityJsons: String) =
        """
        {
            "version": 1,
            "entities": [
                ${entityJsons.joinToString(",\n    ")}
            ]
        }
        """

    private fun bundleJson() =
        buildJson(
            commandJson(),
            automationJson(),
        )

    @Test
    fun `toJson`() {
        val bundle = buildBundle(aCommand(), anAutomation())
        assertJsonEqual(bundleJson(), bundle.toJson())
    }

    @Test
    fun `fromJson`() {
        val bundle = ExportedBundle.fromJson(bundleJson())
        val expected = buildBundle(aCommand(), anAutomation())
        assertEquals(expected.toString(), bundle.toString())
    }

    @Test
    fun `fromJson with unsupported type`() {
        val unknownJson =
            """{"type":"future_type","id":"x","data":"test"}"""
        val bundle = ExportedBundle.fromJson(buildJson(unknownJson))
        val expected = buildBundle(UnknownEntity(unknownJson, "future_type"))
        assertEquals(expected.toString(), bundle.toString())
    }

    @Test
    fun `entityIds returns ids from all entities in bundle`() {
        val bundle = buildBundle(aCommand(), anAutomation())

        val ids = bundle.entityIds()

        assertEquals(setOf("cmd-1", "auto-1"), ids)
    }

    @Test
    fun `command references is empty set`() {
        val cmd = aCommand()

        val refs = cmd.references()

        assertEquals(emptySet<String>(), refs)
    }

    @Test
    fun `automation references with no commands returns trigger only`() {
        val auto = Automation(
            id = "auto-1",
            name = "test",
            triggerType = "trigger_shortcut",
            triggerId = "trg-1",
            commandIds = emptyList(),
        )

        val refs = auto.references()

        assertEquals(setOf("trg-1"), refs)
    }
}
