package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Test

class ExportedBundleTest {
    private fun aCommand(id: String = "cmd-1") =
        LaunchActivityCommand(
            id = id,
            displayName = "Test Command",
            packageName = "com.test",
            className = "com.test.Main",
        )

    private fun commandJson() =
        """{"id": "cmd-1", "displayName": "Test Command", "packageName":"com.test","className":"com.test.Main","extras":{},"flagList":[],"type":"launch_activity"}"""

    private fun anAutomation(
        id: String = "auto-1",
        triggerId: String = "trg-1",
        commandIds: List<String> = listOf("cmd-1"),
    ) = Automation(
        id = id,
        name = "Test Automation",
        triggerType = "trigger_shortcut",
        triggerId = triggerId,
        commandIds = commandIds,
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
        val auto =
            Automation(
                id = "auto-1",
                name = "test",
                triggerType = "trigger_shortcut",
                triggerId = "trg-1",
                commandIds = emptyList(),
            )

        val refs = auto.references()

        assertEquals(setOf("trg-1"), refs)
    }

    @Test
    fun `automation references with one command returns trigger and command`() {
        val auto =
            Automation(
                id = "auto-1",
                name = "test",
                triggerType = "trigger_shortcut",
                triggerId = "trg-1",
                commandIds = listOf("cmd-1"),
            )

        val refs = auto.references()

        assertEquals(setOf("trg-1", "cmd-1"), refs)
    }

    @Test
    fun `automation references with many commands returns trigger and all commands`() {
        val auto =
            Automation(
                id = "auto-1",
                name = "test",
                triggerType = "trigger_shortcut",
                triggerId = "trg-1",
                commandIds = listOf("cmd-1", "cmd-2"),
            )

        val refs = auto.references()

        assertEquals(setOf("trg-1", "cmd-1", "cmd-2"), refs)
    }

    @Test
    fun `bundle references with no automations returns empty set`() {
        val bundle = buildBundle(aCommand(), anTrigger())

        val refs = bundle.references()

        assertEquals(emptySet<String>(), refs)
    }

    @Test
    fun `bundle references with automation returns its refs`() {
        val bundle = buildBundle(anAutomation(), aCommand(), anTrigger())

        val refs = bundle.references()

        assertEquals(setOf("trg-1", "cmd-1"), refs)
    }

    @Test
    fun `bundle references returns union of all automation refs`() {
        val bundle =
            buildBundle(
                anAutomation(),
                anAutomation(id = "auto-2", triggerId = "trg-2", commandIds = listOf("cmd-2")),
                aCommand(),
                anTrigger(),
            )

        val refs = bundle.references()

        assertEquals(setOf("trg-1", "cmd-1", "trg-2", "cmd-2"), refs)
    }

    @Test
    fun `analyzeImport detects overwritten entities`() {
        val bundle =
            buildBundle(
                aCommand(id = "in-both"),
                aCommand(id = "only-bundle"),
            )
        val repoIds = setOf("in-both", "only-repo")

        val analysis = bundle.analyzeImport(repoIds)

        assertEquals(setOf("in-both"), analysis.overwritten)
    }

    @Test
    fun `repositoryReferences includes references to objects in the repository`() {
        val bundle = buildBundle(
            anAutomation(id = "auto-1", triggerId = "ref-in-repo", commandIds = listOf("ref-not-in-repo")),
        )
        val repoIds = setOf("ref-in-repo", "unref-in-repo")

        val analysis = bundle.analyzeImport(repoIds)

        assertEquals(setOf("ref-in-repo"), analysis.repositoryReferences)
    }
}
