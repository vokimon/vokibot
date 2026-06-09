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

    private fun anAutomation() =
        Automation(
            id = "auto-1",
            name = "Test Automation",
            triggerType = "trigger_shortcut",
            triggerId = "trg-1",
            commandIds = listOf("cmd-1"),
        )

    private fun anTrigger() =
        ShortcutTrigger(
            id = "trg-1",
            displayName = "Test Trigger",
        )

    private fun buildBundle(vararg entities: StorableEntity) =
        ExportedBundle(entities = entities.toList())

    private fun bundleJson() ="""
        {
            "version": 1,
            "entities": [
                {"id": "cmd-1", "displayName": "Test Command", "packageName":"com.test","className":"com.test.Main","extras":{},"flagList":[],"type":"launch_activity"},
                {"id": "auto-1", "name": "Test Automation", "triggerType":"trigger_shortcut","triggerId":"trg-1","commandIds":["cmd-1"],"type": "automation"}
            ]
        }
        """

    @Test
    fun `toJson`() {
        val bundle = buildBundle(aCommand(), anAutomation())
        assertJsonEqual(bundleJson(), bundle.toJson())
    }

    @Test
    fun `fromJson`() {
        val expected = buildBundle(aCommand(), anAutomation())
        val bundle = ExportedBundle.fromJson(bundleJson())
        assertEquals(expected.toString(), bundle.toString())
    }

    @Test
    fun `entityIds returns ids from all entities in bundle`() {
        val bundle = buildBundle(aCommand(), anAutomation())

        val ids = bundle.entityIds()

        assertEquals(setOf("cmd-1", "auto-1"), ids)
    }
}
