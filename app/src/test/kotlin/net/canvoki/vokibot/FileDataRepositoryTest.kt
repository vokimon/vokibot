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
        val commandId = "my_command"
        val original = buildCommand()

        repository.saveCommand(commandId, original)
        val retrieved = repository.loadCommand(commandId)

        assertCommandEqual(original, retrieved)
    }

    @Test
    fun loadCommand_notFound_returnsNull() {
        val repository = FileDataRepository(testDir)

        val original = buildCommand()
        repository.saveCommand("id1", original)

        val retrieved = repository.loadCommand("non_existent_id")

        assertCommandEqual(null, retrieved)
    }

    @Test
    fun commandsIsolatedById() {
        val repository = FileDataRepository(testDir)
        val commandA = buildCommand()
        val commandB = buildCommand(displayName = "Different Command")

        repository.saveCommand("id_A", commandA)
        repository.saveCommand("id_B", commandB)
        val retrievedA = repository.loadCommand("id_A")

        assertCommandEqual(commandA, retrievedA)
    }

    @Test
    fun dataPersistence() {
        val repo1 = FileDataRepository(testDir)
        val original = buildCommand()
        repo1.saveCommand("id1", original)

        val repo2 = FileDataRepository(testDir) // Same directory, new instance
        val retrieved = repo2.loadCommand("id1")

        assertCommandEqual(original, retrieved)
    }

    @Test
    fun removeCommand() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand()
        repo.saveCommand("id1", original)
        repo.removeCommand("id1")

        val retrieved = repo.loadCommand("id1")
        assertCommandEqual(null, retrieved)
    }

    @Test
    fun existsComandWhenItExists() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand()
        repo.saveCommand("id1", original)

        val doesExist = repo.existsCommand("id1")
        assertEquals(true, doesExist)
    }

    @Test
    fun existsComandWhenItDoesNotExist() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand()
        repo.saveCommand("id1", original)

        val doesExist = repo.existsCommand("non-existing")
        assertEquals(false, doesExist)
    }

    @Test
    fun listCommandsWhenOnlyOne() {
        val repo = FileDataRepository(testDir)
        val original = buildCommand()
        repo.saveCommand("id1", original)

        val list = repo.listCommands()
        assertEquals(listOf("id1"), list)
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
