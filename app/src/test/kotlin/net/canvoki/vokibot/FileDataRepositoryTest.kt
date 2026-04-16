package net.canvoki.vokibot

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
}
