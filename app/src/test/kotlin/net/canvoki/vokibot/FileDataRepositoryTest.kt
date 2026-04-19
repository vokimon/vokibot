package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class FileDataRepositoryTest {
    private val testDir = "test_repo"

    @Before
    fun setup() {
        File(testDir).deleteRecursively()
    }

    @After
    fun teardown() {
        File(testDir).deleteRecursively()
    }

    @Test
    fun createsDirectoryOnConstruction() {
        assertFalse(
            "The repository dir should NOT exist before creation",
            File(testDir).exists(),
        )

        FileDataRepository(testDir)

        assertTrue(
            "The repository dir should exist after creation",
            File(testDir).exists(),
        )
    }

    @Test
    fun preservesExistingFilesOnConstruction() {
        val existingFile = File(testDir, "existing.txt")
        val originalContent = "original content\nwith multiple lines"

        File(testDir).mkdirs()
        existingFile.writeText(originalContent)

        FileDataRepository(testDir)

        assertEquals(
            originalContent,
            existingFile.readText(),
            "Existing file content should be preserved after repository construction",
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Command Tests
    // ─────────────────────────────────────────────────────────────

    @Test
    fun loadCommand_alreadySaved() {
        val repository = FileDataRepository(testDir)
        val original = buildCommand("my_id")

        repository.saveCommand(original)
        val retrieved = repository.loadCommand("my_id")

        assertDataEqual(original, retrieved)
    }

    @Test
    fun loadCommand_notFound_returnsNull() {
        val repository = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repository.saveCommand(original)

        val retrieved = repository.loadCommand("non_existent_id")

        assertDataEqual(null, retrieved)
    }

    @Test
    fun commandsIsolatedById() {
        val repository = FileDataRepository(testDir)
        val commandA = buildCommand("id_A")
        val commandB = buildCommand("id_B")

        repository.saveCommand(commandA)
        repository.saveCommand(commandB)
        val retrievedA = repository.loadCommand("id_A")

        assertDataEqual(commandA, retrievedA)
    }

    @Test
    fun dataPersistence() {
        val repo1 = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repo1.saveCommand(original)

        val repo2 = FileDataRepository(testDir)
        val retrieved = repo2.loadCommand("id1")

        assertDataEqual(original, retrieved)
    }

    @Test
    fun removeCommand() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repo.saveCommand(original)
        repo.removeCommand("id1")

        val retrieved = repo.loadCommand("id1")
        assertDataEqual(null, retrieved)
    }

    @Test
    fun existsCommandWhenItExists() {
        val repo = FileDataRepository(testDir)
        val command = buildCommand("id1")
        repo.saveCommand(command)

        val doesExist = repo.existsCommand("id1")
        assertEquals(true, doesExist)
    }

    @Test
    fun existsCommandWhenItDoesNotExist() {
        val repo = FileDataRepository(testDir)
        val command = buildCommand("id1")
        repo.saveCommand(command)

        val doesExist = repo.existsCommand("non-existing")
        assertEquals(false, doesExist)
    }

    @Test
    fun listCommands_singleCommand() {
        val repo = FileDataRepository(testDir)
        val command = buildCommand("id1")
        repo.saveCommand(command)

        val list = repo.listCommands()
        assertEquals(listOf("id1"), list)
    }

    @Test
    fun listCommands_noCommands() {
        val repo = FileDataRepository(testDir)
        val list = repo.listCommands()
        assertEquals(emptyList<String>(), list)
    }

    @Test
    fun listCommands_manyCommands() {
        val repo = FileDataRepository(testDir)
        repo.saveCommand(buildCommand("id1"))
        repo.saveCommand(buildCommand("id2"))

        val list = repo.listCommands()
        assertEquals(listOf("id1", "id2"), list)
    }

    @Test
    fun listCommands_ignoresBadPrefix() {
        val repo = FileDataRepository(testDir)
        repo.saveCommand(buildCommand("cmd1"))
        File(testDir, "log.json").writeText("{}")

        assertEquals(listOf("cmd1"), repo.listCommands())
    }

    @Test
    fun listCommands_ignoresBadSuffix() {
        val repo = FileDataRepository(testDir)
        repo.saveCommand(buildCommand("cmd1"))
        File(testDir, "command_log.txt").writeText("{}")

        assertEquals(listOf("cmd1"), repo.listCommands())
    }

    @Test
    fun loadAllCommands_returnsSingleSavedCommand() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repo.saveCommand(original)

        val loaded = repo.loadAllCommands()
        assertDataEqual(original, loaded.firstOrNull())
    }

    @Test
    fun loadAllCommands_returnsManySavedCommands() {
        val repo = FileDataRepository(testDir)
        val data1 = buildCommand("id1")
        val data2 = buildCommand("id2")
        repo.saveCommand(data1)
        repo.saveCommand(data2)

        val loaded = repo.loadAllCommands()
        assertDataEqual(data1, loaded.firstOrNull())
        assertDataEqual(data2, loaded.lastOrNull())
    }

    // ─────────────────────────────────────────────────────────────
    // NFC Trigger Tests (mirroring command tests)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun loadNfcTrigger_alreadySaved() {
        val repository = FileDataRepository(testDir)
        val original = buildNfc("my tag", "10:01")

        repository.saveNfcTrigger(original)
        val retrieved = repository.loadNfcTrigger("10:01")

        assertDataEqual(original, retrieved)
    }

    @Test
    fun loadNfcTrigger_notFound_returnsNull() {
        val repository = FileDataRepository(testDir)
        val original = buildNfc("tag", "10:01")
        repository.saveNfcTrigger(original)

        val retrieved = repository.loadNfcTrigger("non_existent")

        assertDataEqual(null, retrieved)
    }

    @Test
    fun nfcTriggersIsolatedById() {
        val repository = FileDataRepository(testDir)
        val triggerA = buildNfc("tag A", "AA:01")
        val triggerB = buildNfc("tag B", "BB:02")

        repository.saveNfcTrigger(triggerA)
        repository.saveNfcTrigger(triggerB)
        val retrievedA = repository.loadNfcTrigger("AA:01")

        assertDataEqual(triggerA, retrievedA)
    }

    @Test
    fun removeNfcTrigger() {
        val repo = FileDataRepository(testDir)
        val original = buildNfc("tag", "10:01")
        repo.saveNfcTrigger(original)
        repo.removeNfcTrigger("10:01")

        val retrieved = repo.loadNfcTrigger("10:01")
        assertDataEqual(null, retrieved)
    }

    @Test
    fun existsNfcTriggerWhenItExists() {
        val repo = FileDataRepository(testDir)
        val trigger = buildNfc("tag", "10:01")
        repo.saveNfcTrigger(trigger)

        val doesExist = repo.existsNfcTrigger("10:01")
        assertEquals(true, doesExist)
    }

    @Test
    fun existsNfcTriggerWhenItDoesNotExist() {
        val repo = FileDataRepository(testDir)
        val trigger = buildNfc("tag", "10:01")
        repo.saveNfcTrigger(trigger)

        val doesExist = repo.existsNfcTrigger("non-existing")
        assertEquals(false, doesExist)
    }

    @Test
    fun listNfcTriggers_singleTrigger() {
        val repo = FileDataRepository(testDir)
        val trigger = buildNfc("tag", "10:01")
        repo.saveNfcTrigger(trigger)

        val list = repo.listNfcTriggers()
        assertEquals(listOf("10_01"), list)
    }

    @Test
    fun listNfcTriggers_noTriggers() {
        val repo = FileDataRepository(testDir)
        val list = repo.listNfcTriggers()
        assertEquals(emptyList<String>(), list)
    }

    @Test
    fun listNfcTriggers_manyTriggers() {
        val repo = FileDataRepository(testDir)
        repo.saveNfcTrigger(buildNfc("tag1", "10:01"))
        repo.saveNfcTrigger(buildNfc("tag2", "20:02"))

        val list = repo.listNfcTriggers()
        assertEquals(listOf("10_01", "20_02"), list.sorted())
    }

    @Test
    fun listNfcTriggers_ignoresBadPrefix() {
        val repo = FileDataRepository(testDir)
        repo.saveNfcTrigger(buildNfc("tag", "10:01"))
        File(testDir, "log.json").writeText("{}")

        assertEquals(listOf("10_01"), repo.listNfcTriggers())
    }

    @Test
    fun listNfcTriggers_ignoresBadSuffix() {
        val repo = FileDataRepository(testDir)
        repo.saveNfcTrigger(buildNfc("tag", "10:01"))
        File(testDir, "trigger_nfc_log.txt").writeText("{}")

        assertEquals(listOf("10_01"), repo.listNfcTriggers())
    }

    @Test
    fun loadAllNfcTriggers_returnsSingleSavedTrigger() {
        val repo = FileDataRepository(testDir)
        val original = buildNfc("tag", "10:01")
        repo.saveNfcTrigger(original)

        val loaded = repo.loadAllNfcTriggers()
        assertDataEqual(original, loaded.firstOrNull())
    }

    @Test
    fun loadAllNfcTriggers_returnsManySavedTriggers() {
        val repo = FileDataRepository(testDir)
        val data1 = buildNfc("tag1", "10:01")
        val data2 = buildNfc("tag2", "20:02")
        repo.saveNfcTrigger(data1)
        repo.saveNfcTrigger(data2)

        val loaded = repo.loadAllNfcTriggers()
        assertDataEqual(data1, loaded.firstOrNull())
        assertDataEqual(data2, loaded.lastOrNull())
    }
}

fun <T : StorableEntity> assertDataEqual(
    expected: T?,
    actual: T?,
) {
    assertJsonEqual(
        expected?.toJson() ?: "null",
        actual?.toJson() ?: "null",
    )
}

private fun buildCommand(displayName: String = "Test Command"): ApplicationCommand =
    LaunchActivityCommand(
        displayName = displayName,
        packageName = "com.test.pkg",
        className = "com.test.pkg.MainActivity",
    )

private fun buildNfc(
    name: String,
    uid: String,
): NfcTrigger =
    NfcTrigger(
        displayName = name,
        uid = uid,
    )
