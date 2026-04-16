package net.canvoki.vokibot

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import net.canvoki.vokibot.ActionFilterType
import net.canvoki.vokibot.ComponentType
import net.canvoki.vokibot.PublicComponent
import net.canvoki.vokibot.PublicComponentsResult
import net.canvoki.shared.test.canonicalizeJson

/**
 * Formats a component's parsed data into a canonical JSON string for snapshot-style testing.
 * Uses shared [canonicalizeJson] for deterministic key order and pretty-printing.
 *
 * TODO: Add dataSchemes, mimeTypes, permissions when properly supported
 */
fun PublicComponentsResult.formatComponentJson(componentName: String): String {
    val comp = components.find { it.name == componentName || it.name.endsWith(componentName) }
        ?: return buildJsonObject {
            put("found", false)
            put("message", "Component '$componentName' not found")
        }.let { canonicalizeJson(it.toString()) }

    return buildJsonObject {
        put("found", true)
        put("name", comp.name)
        put("type", comp.type.name)
        put("exported", comp.exported)
        put("label", comp.label)

        putJsonObject("actions") {
            put("filterType", comp.actionFilterType.name)
            when (comp.actionFilterType) {
                ActionFilterType.SPECIFIC_ACTIONS ->
                    putJsonArray("values") {
                        comp.actions.forEach { add(JsonPrimitive(it)) }
                    }
                else -> {}
            }
        }
    }.let { canonicalizeJson(it.toString()) }
}

/**
 * Asserts that a component's parsed info matches the expected JSON structure.
 * Uses [canonicalizeJson] for normalization, so key order and whitespace don't matter.
 */
fun assertComponentInfoEquals(
    expected: String,
    actual: PublicComponentsResult,
    componentName: String,
    message: String = "",
) {
    val normalizedExpected = canonicalizeJson(expected)
    val normalizedActual = actual.formatComponentJson(componentName)
    assertEquals(normalizedExpected, normalizedActual)
}

@RunWith(AndroidJUnit4::class)
class ComponentDiscoveryTest {

    @Before
    fun setup() {
        PuppetHelper.requireInstalled()
    }

    @Test
    fun mainActivity_snapshot() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = queryPublicComponents(context, "net.canvoki.puppet", exportedOnly = true)

        val expected = """
        {
          "actions": {
            "filterType": "SPECIFIC_ACTIONS",
            "values": ["android.intent.action.MAIN"]
          },
          "exported": true,
          "found": true,
          "label": "0_Puppet",
          "name": "net.canvoki.puppet.MainActivity",
          "type": "ACTIVITY"
        }
        """.trimIndent()

        assertComponentInfoEquals(expected, result, ".MainActivity")
    }

    @Test
    fun unfilteredActivity_snapshot() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = queryPublicComponents(context, "net.canvoki.puppet", exportedOnly = true)

        val expected = """
        {
          "actions": {
            "filterType": "UNKNOWN"
          },
          "exported": true,
          "found": true,
          "label": "Unfiltered",
          "name": "net.canvoki.puppet.UnfilteredActivity",
          "type": "ACTIVITY"
        }
        """.trimIndent()

        assertComponentInfoEquals(expected, result, ".UnfilteredActivity")
    }

    @Test
    fun privateActivity_excludedWhenExportedOnly() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = queryPublicComponents(context, "net.canvoki.puppet", exportedOnly = true)

        val expected = """
        {
          "found": false,
          "message": "Component '.PrivateActivity' not found"
        }
        """.trimIndent()

        assertComponentInfoEquals(expected, result, ".PrivateActivity")
    }
}
