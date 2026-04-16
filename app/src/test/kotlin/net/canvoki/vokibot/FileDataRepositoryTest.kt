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
    fun saveAndLoadCommand_roundTrip() {
        val repository = FileDataRepository(testDir)
        val commandId = "my_command"
        val original = defaultCommand()

        repository.saveCommand(commandId, original)
        val retrieved = repository.loadCommand(commandId)

        assertCommandEqual(original, retrieved)
    }

    @Test
    fun saveAndLoadCommand_differentIdsAreIsolated() {
        val repository = FileDataRepository(testDir)
        val idA = "cmd_a"
        val idB = "cmd_b"
        val commandA = defaultCommand()
        val commandB = defaultCommand(displayName = "Different Command")

        repository.saveCommand(idA, commandA)
        repository.saveCommand(idB, commandB)
        val retrievedA = repository.loadCommand(idA)

        assertCommandEqual(commandA, retrievedA)
    }

}


private fun defaultCommand(
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
