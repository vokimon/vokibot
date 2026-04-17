package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import net.canvoki.shared.test.assertJsonEqual

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

        // Setup: create directory and file with known content BEFORE repository construction
        File(testDir).mkdirs()
        existingFile.writeText(originalContent)

        // Act: construct repository (should not mutate existing files)
        FileDataRepository(testDir)

        // Assert: file content unchanged
        assertEquals(
            originalContent,
            existingFile.readText(),
            "Existing file content should be preserved after repository construction",
        )
    }

    @Test
    fun loadCommand_alreadySaved() {
        val repository = FileDataRepository(testDir)
        val original = buildCommand("my_id")

        repository.saveCommand(original)
        val retrieved = repository.loadCommand("my_id")

        assertCommandEqual(original, retrieved)
    }

    @Test
    fun loadCommand_notFound_returnsNull() {
        val repository = FileDataRepository(testDir)

        val original = buildCommand("id1")
        repository.saveCommand(original)

        val retrieved = repository.loadCommand("non_existent_id")

        assertCommandEqual(null, retrieved)
    }

    @Test
    fun commandsIsolatedById() {
        val repository = FileDataRepository(testDir)
        val commandA = buildCommand("id_A")
        val commandB = buildCommand("id_B")

        repository.saveCommand(commandA)
        repository.saveCommand(commandB)
        val retrievedA = repository.loadCommand("id_A")

        assertCommandEqual(commandA, retrievedA)
    }

    @Test
    fun dataPersistence() {
        val repo1 = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repo1.saveCommand(original)

        val repo2 = FileDataRepository(testDir) // Same directory, new instance
        val retrieved = repo2.loadCommand("id1")

        assertCommandEqual(original, retrieved)
    }

    @Test
    fun removeCommand() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand("id1")
        repo.saveCommand(original)
        repo.removeCommand("id1")

        val retrieved = repo.loadCommand("id1")
        assertCommandEqual(null, retrieved)
    }

    @Test
    fun existsComandWhenItExists() {
        val repo = FileDataRepository(testDir)
        val command = buildCommand("id1")
        repo.saveCommand(command)

        val doesExist = repo.existsCommand("id1")
        assertEquals(true, doesExist)
    }

    @Test
    fun existsComandWhenItDoesNotExist() {
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
        assertEquals(listOf("id1","id2"), list)
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
        assertCommandEqual(original, loaded.firstOrNull())
    }
}


private fun buildCommand(
    displayName: String =  "Test Command",
): ApplicationCommand = LaunchActivityCommand(
    displayName = displayName,
    packageName = "com.test.pkg",
    className = "com.test.pkg.MainActivity"
)

fun assertCommandEqual(expected: ApplicationCommand?, actual: ApplicationCommand?) {
    assertJsonEqual(
        expected?.toJson() ?: "null",
        actual?.toJson() ?: "null",
    )
}
