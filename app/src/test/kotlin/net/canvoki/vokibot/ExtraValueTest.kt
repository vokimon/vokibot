package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.KClass

class ExtraValueTest {
    // ---------- defaultValue ----------

    private fun checkDefaultValue(
        spec: ExtraSpec,
        expectedClass: KClass<*>,
    ) = assertEquals(expectedClass, spec.defaultValue()::class)

    @Test
    fun `defaultValue for URI returns UriValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.URI), ExtraValue.UriValue::class)
    }

    @Test
    fun `defaultValue for STRING returns StringValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.STRING), ExtraValue.StringValue::class)
    }

    @Test
    fun `defaultValue for INT returns IntValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.INT), ExtraValue.IntValue::class)
    }

    @Test
    fun `defaultValue for BOOLEAN returns BooleanValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.BOOLEAN), ExtraValue.BooleanValue::class)
    }

    @Test
    fun `defaultValue for STRING_ARRAY returns StringArrayValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.STRING_ARRAY), ExtraValue.StringArrayValue::class)
    }

    @Test
    fun `defaultValue for URI_LIST returns UriListValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.URI_LIST), ExtraValue.UriListValue::class)
    }

    // ---------- Serialization discriminators ----------

    @Test
    fun `ExtraValue polymorphic serialization`() {
        val extras =
            mapOf(
                "str" to ExtraValue.StringValue("hello"),
                "num" to ExtraValue.IntValue(42),
                "lng" to ExtraValue.LongValue(123L),
                "bool" to ExtraValue.BooleanValue(true),
                "flt" to ExtraValue.FloatValue(3.14f),
            )

        val serialized = JsonConfig.encodeToString(extras)
        assertTrue(serialized.contains("\"string\""))
        assertTrue(serialized.contains("\"int\""))
        assertTrue(serialized.contains("\"long\""))
        assertTrue(serialized.contains("\"boolean\""))
        assertTrue(serialized.contains("\"float\""))

        val deserialized = JsonConfig.decodeFromString<Map<String, ExtraValue>>(serialized)
        assertEquals(
            (extras["str"] as ExtraValue.StringValue).value,
            (deserialized["str"] as ExtraValue.StringValue).value,
        )
        assertEquals((extras["num"] as ExtraValue.IntValue).value, (deserialized["num"] as ExtraValue.IntValue).value)
        assertEquals(
            (extras["bool"] as ExtraValue.BooleanValue).value,
            (deserialized["bool"] as ExtraValue.BooleanValue).value,
        )
    }

    @Test
    fun `StringValue serializes with string discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.StringValue("hello") as ExtraValue)
        assertJsonEqual("""{"type": "string", "value": "hello"}""", json)
    }

    @Test
    fun `IntValue serializes with int discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.IntValue(42) as ExtraValue)
        assertJsonEqual("""{"type": "int", "value": 42}""", json)
    }

    @Test
    fun `LongValue serializes with long discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.LongValue(123L) as ExtraValue)
        assertJsonEqual("""{"type": "long", "value": 123}""", json)
    }

    @Test
    fun `BooleanValue serializes with boolean discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.BooleanValue(true) as ExtraValue)
        assertJsonEqual("""{"type": "boolean", "value": true}""", json)
    }

    @Test
    fun `FloatValue serializes with float discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.FloatValue(3.14f) as ExtraValue)
        assertJsonEqual("""{"type": "float", "value": 3.14}""", json)
    }

    @Test
    fun `UriValue serializes with uri discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.UriValue("geo:0,0") as ExtraValue)
        assertJsonEqual("""{"type": "uri", "value": "geo:0,0"}""", json)
    }

    @Test
    fun `StringArrayValue serializes with string_array discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.StringArrayValue(listOf("a")) as ExtraValue)
        assertJsonEqual("""{"type": "string_array", "values": ["a"]}""", json)
    }

    @Test
    fun `UriListValue serializes with uri_list discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.UriListValue(listOf("geo:0,0")) as ExtraValue)
        assertJsonEqual("""{"type": "uri_list", "values": ["geo:0,0"]}""", json)
    }

    // ---------- isDefault ----------

    private fun checkDefault(
        value: ExtraValue,
        expectDefault: Boolean,
    ) = assertEquals(expectDefault, value.isDefault())

    @Test
    fun `StringValue with empty string is default`() {
        checkDefault(ExtraValue.StringValue(""), true)
    }

    @Test
    fun `StringValue with non-empty string is not default`() {
        checkDefault(ExtraValue.StringValue("hello"), false)
    }

    @Test
    fun `IntValue with zero is default`() {
        checkDefault(ExtraValue.IntValue(0), true)
    }

    @Test
    fun `IntValue with non-zero is not default`() {
        checkDefault(ExtraValue.IntValue(42), false)
    }

    @Test
    fun `BooleanValue with false is default`() {
        checkDefault(ExtraValue.BooleanValue(false), true)
    }

    @Test
    fun `BooleanValue with true is not default`() {
        checkDefault(ExtraValue.BooleanValue(true), false)
    }

    @Test
    fun `UriValue with empty string is default`() {
        checkDefault(ExtraValue.UriValue(""), true)
    }

    @Test
    fun `UriValue with non-empty string is not default`() {
        checkDefault(ExtraValue.UriValue("geo:0,0"), false)
    }

    @Test
    fun `StringArrayValue with empty list is default`() {
        checkDefault(ExtraValue.StringArrayValue(emptyList()), true)
    }

    @Test
    fun `StringArrayValue with non-empty list is not default`() {
        checkDefault(ExtraValue.StringArrayValue(listOf("a")), false)
    }

    @Test
    fun `UriListValue with empty list is default`() {
        checkDefault(ExtraValue.UriListValue(emptyList()), true)
    }

    @Test
    fun `UriListValue with non-empty list is not default`() {
        checkDefault(ExtraValue.UriListValue(listOf("geo:0,0")), false)
    }

    // ---------- toExtraType ----------

    private fun checkExtraType(
        value: ExtraValue,
        expected: ExtraType,
    ) = assertEquals(expected, value.toExtraType())

    @Test
    fun `StringValue toExtraType is STRING`() {
        checkExtraType(ExtraValue.StringValue(""), ExtraType.STRING)
    }

    @Test
    fun `IntValue toExtraType is INT`() {
        checkExtraType(ExtraValue.IntValue(0), ExtraType.INT)
    }

    @Test
    fun `LongValue toExtraType is INT`() {
        checkExtraType(ExtraValue.LongValue(0L), ExtraType.INT)
    }

    @Test
    fun `BooleanValue toExtraType is BOOLEAN`() {
        checkExtraType(ExtraValue.BooleanValue(false), ExtraType.BOOLEAN)
    }

    @Test
    fun `FloatValue toExtraType is STRING`() {
        checkExtraType(ExtraValue.FloatValue(0f), ExtraType.STRING)
    }

    @Test
    fun `UriValue toExtraType is URI`() {
        checkExtraType(ExtraValue.UriValue(""), ExtraType.URI)
    }

    @Test
    fun `StringArrayValue toExtraType is STRING_ARRAY`() {
        checkExtraType(ExtraValue.StringArrayValue(emptyList()), ExtraType.STRING_ARRAY)
    }

    @Test
    fun `UriListValue toExtraType is URI_LIST`() {
        checkExtraType(ExtraValue.UriListValue(emptyList()), ExtraType.URI_LIST)
    }

    // ---------- toPersistedString ----------

    @Test
    fun `StringValue toPersistedString returns value`() {
        assertEquals("hello", ExtraValue.StringValue("hello").toPersistedString())
    }

    @Test
    fun `StringValue toPersistedString empty returns empty`() {
        assertEquals("", ExtraValue.StringValue("").toPersistedString())
    }

    @Test
    fun `IntValue toPersistedString returns number as string`() {
        assertEquals("42", ExtraValue.IntValue(42).toPersistedString())
    }

    @Test
    fun `IntValue toPersistedString zero`() {
        assertEquals("0", ExtraValue.IntValue(0).toPersistedString())
    }

    @Test
    fun `LongValue toPersistedString returns number as string`() {
        assertEquals("123", ExtraValue.LongValue(123L).toPersistedString())
    }

    @Test
    fun `BooleanValue toPersistedString true`() {
        assertEquals("1", ExtraValue.BooleanValue(true).toPersistedString())
    }

    @Test
    fun `BooleanValue toPersistedString false`() {
        assertEquals("0", ExtraValue.BooleanValue(false).toPersistedString())
    }

    @Test
    fun `FloatValue toPersistedString returns number as string`() {
        assertEquals("3.14", ExtraValue.FloatValue(3.14f).toPersistedString())
    }

    @Test
    fun `UriValue toPersistedString returns value`() {
        assertEquals("geo:0,0", ExtraValue.UriValue("geo:0,0").toPersistedString())
    }

    @Test
    fun `StringArrayValue toPersistedString returns comma-separated`() {
        assertEquals("a,b", ExtraValue.StringArrayValue(listOf("a", "b")).toPersistedString())
    }

    @Test
    fun `StringArrayValue toPersistedString empty returns empty`() {
        assertEquals("", ExtraValue.StringArrayValue(emptyList()).toPersistedString())
    }

    @Test
    fun `UriListValue toPersistedString returns comma-separated`() {
        assertEquals("geo:0,0,tel:123", ExtraValue.UriListValue(listOf("geo:0,0", "tel:123")).toPersistedString())
    }

    @Test
    fun `UriListValue toPersistedString empty returns empty`() {
        assertEquals("", ExtraValue.UriListValue(emptyList()).toPersistedString())
    }

    // ---------- computeNewCustomSpecs ----------

    private fun assertSpecs(
        expected: String,
        actual: List<ExtraSpec>,
    ) = assertEquals(
        expected,
        actual.joinToString("\n") { "${it.key}: ${it.type}" },
    )

    @Test
    fun `computeNewCustomSpecs no data, returns empty list`() {
        assertSpecs(
            expected = "",
            actual =
                computeNewCustomSpecs(
                    extrasState = emptyMap(),
                    newActionExtras = emptyList(),
                ),
        )
    }

    @Test
    fun `computeNewCustomSpecs not in command, include it`() {
        assertSpecs(
            expected = "extra1: STRING",
            actual =
                computeNewCustomSpecs(
                    extrasState = mapOf("extra1" to ExtraValue.StringValue("value1")),
                    newActionExtras = emptyList(),
                ),
        )
    }

    @Test
    fun `computeNewCustomSpecs already in command, exclude it`() {
        assertSpecs(
            expected = "",
            actual =
                computeNewCustomSpecs(
                    extrasState = mapOf("extra1" to ExtraValue.StringValue("value1")),
                    newActionExtras = listOf(ExtraSpec("extra1", ExtraType.STRING)),
                ),
        )
    }

    @Test
    fun `computeNewCustomSpecs not in command but default value, exclude it`() {
        assertSpecs(
            expected = "",
            actual =
                computeNewCustomSpecs(
                    extrasState = mapOf("extra1" to ExtraValue.StringValue("")),
                    newActionExtras = emptyList(),
                ),
        )
    }

    @Test
    fun `computeNewCustomSpecs multiple orphans not in command, include all`() {
        assertSpecs(
            expected = "extra1: STRING\nextra2: INT",
            actual =
                computeNewCustomSpecs(
                    extrasState =
                        mapOf(
                            "extra1" to ExtraValue.StringValue("value1"),
                            "extra2" to ExtraValue.IntValue(42),
                        ),
                    newActionExtras = emptyList(),
                ),
        )
    }

    // ---------- rebuildExtras ----------

    private fun assertRebuiltExtras(
        values: Map<String, ExtraValue>,
        actionSpecs: List<ExtraSpec>,
        customSpecs: List<ExtraSpec>,
        expected: String,
    ) {
        val result =
            rebuildExtras(
                values = values,
                actionSpecs = actionSpecs,
                customSpecs = customSpecs,
            )
        assertJsonEqual(expected, JsonConfig.encodeToString(result))
    }

    @Test
    fun `rebuildExtras with empty specs returns empty`() {
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs = emptyList(),
            customSpecs = emptyList(),
            expected = "{}",
        )
    }

    @Test
    fun `rebuildExtras keys in action spec are preserved`() {
        assertRebuiltExtras(
            values =
                mapOf(
                    "extra1" to ExtraValue.StringValue("value1"),
                ),
            actionSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.STRING),
                ),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "string", "value": "value1"}}""",
        )
    }

    @Test
    fun `rebuildExtras multiple existing keys are preserved`() {
        assertRebuiltExtras(
            values =
                mapOf(
                    "extra1" to ExtraValue.StringValue("value1"),
                    "extra2" to ExtraValue.IntValue(666),
                ),
            actionSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.STRING),
                    ExtraSpec("extra2", ExtraType.INT),
                ),
            customSpecs = emptyList(),
            expected =
                """{"extra1": {"type": "string", "value": "value1"},""" +
                    """ "extra2": {"type": "int", "value": 666}}""",
        )
    }

    @Test
    fun `rebuildExtras action specs not in values get default values`() {
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.STRING),
                ),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "string", "value": ""}}""",
        )
    }

    @Test
    fun `rebuildExtras default is type aware`() {
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs = listOf(ExtraSpec("extra1", ExtraType.INT)),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "int", "value": 0}}""",
        )
    }

    @Test
    fun `rebuildExtras uses custom specs as it uses action specs`() {
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs = emptyList(),
            customSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.STRING),
                ),
            expected = """{"extra1": {"type": "string", "value": ""}}""",
        )
    }

    @Test
    fun `rebuildExtras conflicting action and custom specs, custom is taken`() {
        // This behaviour is not specified, just tested to document what it does.
        // computeNewCustomSpecs ensures both specs are disjoint.
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.STRING),
                ),
            customSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.INT),
                ),
            expected = """{"extra1": {"type": "int", "value": 0}}""",
        )
    }

    @Test
    fun `rebuildExtras when values does not match spec type, reset to default`() {
        assertRebuiltExtras(
            values =
                mapOf(
                    "extra1" to ExtraValue.StringValue("value1"),
                ),
            actionSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.INT),
                ),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "int", "value": 0}}""",
        )
    }
}
