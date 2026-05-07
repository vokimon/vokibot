package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import org.junit.Test

fun assertRegisteredTypes(expected: String, actual: List<EntityTypeInfo>) {
    val actualString = actual.map { it.typeKey }.sorted().joinToString("\n")
    assertEquals(expected, actualString)
}

class EntityRegistryTest {
    @Test
    fun `empty registry returns empty`() {
        val registry = EntityRegistry()
        assertRegisteredTypes("", registry.getRegisteredTypes())
    }
}
