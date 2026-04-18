package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import net.canvoki.shared.test.assertJsonEqual

class DataSetTest {
    private val testDir = "test_dataset"
    private val dir = File(testDir)

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
            "The dataset dir should NOT exist before creation",
            File(testDir).exists(),
        )

        val dataSet = createDataSet()

        assertTrue(
            "The dataset dir should exist after creation",
            File(testDir).exists(),
        )
    }

    fun createDataSet(): DataSet<ApplicationCommand> =
        DataSet<ApplicationCommand>(dir, "command_", ApplicationCommand::fromJson)

    @Test
    fun preservesExistingFilesOnConstruction() {
        val existingFile = File(testDir, "existing.txt")
        val originalContent = "original content\nwith multiple lines"

        File(testDir).mkdirs()
        existingFile.writeText(originalContent)

        val dataSet = createDataSet()

        assertEquals(
            originalContent,
            existingFile.readText(),
            "Existing file content should be preserved after dataset construction",
        )
    }

    @Test
    fun load_alreadySaved() {
        val dataSet = createDataSet()
        val original = buildCommand("my_id")

        dataSet.save(original)
        val retrieved = dataSet.load("my_id")

        assertDataEqual(original, retrieved)
    }

    @Test
    fun load_notFound_returnsNull() {
        val dataSet = createDataSet()
        val original = buildCommand("id1")
        dataSet.save(original)

        val retrieved = dataSet.load("non_existent_id")

        assertDataEqual(null, retrieved)
    }

    @Test
    fun itemsIsolatedById() {
        val dataSet = createDataSet()
        val commandA = buildCommand("id_A")
        val commandB = buildCommand("id_B")

        dataSet.save(commandA)
        dataSet.save(commandB)
        val retrievedA = dataSet.load("id_A")

        assertDataEqual(commandA, retrievedA)
    }

    @Test
    fun dataPersistence() {
        val original = buildCommand("id1")

        val dataSet1 = createDataSet()
        dataSet1.save(original)

        val dataSet2 = createDataSet()
        val retrieved = dataSet2.load("id1")

        assertDataEqual(original, retrieved)
    }

    @Test
    fun remove() {
        val dataSet = createDataSet()
        val original = buildCommand("id1")
        dataSet.save(original)
        dataSet.remove("id1")

        val retrieved = dataSet.load("id1")
        assertDataEqual(null, retrieved)
    }

    @Test
    fun existsWhenItExists() {
        val dataSet = createDataSet()
        val command = buildCommand("id1")
        dataSet.save(command)

        val doesExist = File(dir, "command_id1.json").exists()
        assertEquals(true, doesExist)
    }

    @Test
    fun existsWhenItDoesNotExist() {
        val dataSet = createDataSet()
        val command = buildCommand("id1")
        dataSet.save(command)

        val doesExist = File(dir, "command_non_existing.json").exists()
        assertEquals(false, doesExist)
    }

    @Test
    fun listIds_singleItem() {
        val dataSet = createDataSet()
        val command = buildCommand("id1")
        dataSet.save(command)

        val list = dataSet.listIds()
        assertEquals(listOf("id1"), list)
    }

    @Test
    fun listIds_noItems() {
        val dataSet = createDataSet()

        val list = dataSet.listIds()
        assertEquals(emptyList<String>(), list)
    }

    @Test
    fun listIds_manyItems() {
        val dataSet = createDataSet()

        dataSet.save(buildCommand("id1"))
        dataSet.save(buildCommand("id2"))

        val list = dataSet.listIds()
        assertEquals(listOf("id1", "id2"), list.sorted())
    }

    @Test
    fun listIds_ignoresBadPrefix() {
        val dataSet = createDataSet()
        dataSet.save(buildCommand("cmd1"))
        File(dir, "log.json").writeText("{}")

        assertEquals(listOf("cmd1"), dataSet.listIds())
    }

    @Test
    fun listIds_ignoresBadSuffix() {
        val dataSet = createDataSet()
        dataSet.save(buildCommand("cmd1"))
        File(dir, "command_log.txt").writeText("{}")

        assertEquals(listOf("cmd1"), dataSet.listIds())
    }

    @Test
    fun all_returnsSingleSavedItem() {
        val dataSet = createDataSet()
        val data1 = buildCommand("id1")
        dataSet.save(data1)

        val loaded = dataSet.all()
        assertDataEqual(data1, loaded.firstOrNull())
    }

    @Test
    fun all_returnsManySavedItem() {
        val dataSet = createDataSet()
        val data1 = buildCommand("id1")
        val data2 = buildCommand("id2")
        dataSet.save(data1)
        dataSet.save(data2)

        val loaded = dataSet.all()
        assertDataEqual(data1, loaded.firstOrNull())
        assertDataEqual(data2, loaded.lastOrNull())
    }

}

private fun buildCommand(
    displayName: String = "Test Command",
): ApplicationCommand = LaunchActivityCommand(
    displayName = displayName,
    packageName = "com.test.pkg",
    className = "com.test.pkg.MainActivity"
)

fun assertDataEqual(expected: StorableEntity?, actual: StorableEntity?) {
    assertJsonEqual(
        expected?.toJson() ?: "null",
        actual?.toJson() ?: "null",
    )
}
