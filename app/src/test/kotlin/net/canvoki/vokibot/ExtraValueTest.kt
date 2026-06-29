package net.canvoki.vokibot

import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.common.FlagSerialization
import net.canvoki.vokibot.common.SelectableOption
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
        checkDefaultValue(ExtraSpec("k", ExtraType.Uri), ExtraValue.UriValue::class)
    }

    @Test
    fun `defaultValue for STRING returns StringValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.String), ExtraValue.StringValue::class)
    }

    @Test
    fun `defaultValue for INT returns IntValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.Int()), ExtraValue.IntValue::class)
    }

    @Test
    fun `defaultValue for BOOLEAN returns BooleanValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.Boolean), ExtraValue.BooleanValue::class)
    }

    @Test
    fun `defaultValue for STRING_ARRAY returns StringArrayValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.StringArray), ExtraValue.StringArrayValue::class)
    }

    @Test
    fun `defaultValue for URI_LIST returns UriListValue`() {
        checkDefaultValue(ExtraSpec("k", ExtraType.UriList), ExtraValue.UriListValue::class)
    }

    // ---------- Serialization discriminators ----------

    @Test
    fun `ExtraValue polymorphic serialization`() {
        val extras =
            mapOf(
                "str" to ExtraValue.StringValue("hello"),
                "num" to ExtraValue.IntValue(42),
                "bool" to ExtraValue.BooleanValue(true),
            )

        val serialized = JsonConfig.encodeToString(extras)
        assertTrue(serialized.contains("\"string\""))
        assertTrue(serialized.contains("\"int\""))
        assertTrue(serialized.contains("\"boolean\""))

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
    fun `BooleanValue serializes with boolean discriminator`() {
        val json = JsonConfig.encodeToString(ExtraValue.BooleanValue(true) as ExtraValue)
        assertJsonEqual("""{"type": "boolean", "value": true}""", json)
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

    // ---------- getExtraType ----------

    private fun checkExtraType(
        value: ExtraValue,
        expected: ExtraType,
    ) = assertEquals(expected, value.getExtraType())

    @Test
    fun `StringValue getExtraType is STRING`() {
        checkExtraType(ExtraValue.StringValue(""), ExtraType.String)
    }

    @Test
    fun `IntValue getExtraType is INT`() {
        checkExtraType(ExtraValue.IntValue(0), ExtraType.Int())
    }

    @Test
    fun `BooleanValue getExtraType is BOOLEAN`() {
        checkExtraType(ExtraValue.BooleanValue(false), ExtraType.Boolean)
    }

    @Test
    fun `UriValue getExtraType is URI`() {
        checkExtraType(ExtraValue.UriValue(""), ExtraType.Uri)
    }

    @Test
    fun `StringArrayValue getExtraType is STRING_ARRAY`() {
        checkExtraType(ExtraValue.StringArrayValue(emptyList()), ExtraType.StringArray)
    }

    @Test
    fun `UriListValue getExtraType is URI_LIST`() {
        checkExtraType(ExtraValue.UriListValue(emptyList()), ExtraType.UriList)
    }

    // ---------- toStoredSetting ----------

    private fun checkTypeStoredValue(
        type: ExtraType,
        value: ExtraValue,
        expected: String,
    ) = assertEquals(expected, type.toStoredSetting(value))

    private fun checkTypeStoredValueDefault(
        type: ExtraType,
        wrongValue: ExtraValue,
    ) = checkTypeStoredValue(type, wrongValue, type.toStoredSetting(type.defaultValue()))

    @Test
    fun `StringValue toStoredSetting returns value`() {
        checkTypeStoredValue(ExtraType.String, ExtraValue.StringValue("hello"), "hello")
    }

    @Test
    fun `StringValue toStoredSetting empty returns empty`() {
        checkTypeStoredValue(ExtraType.String, ExtraValue.StringValue(""), "")
    }

    @Test
    fun `StringValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.String, ExtraValue.IntValue(0))
    }

    @Test
    fun `IntValue toStoredSetting returns number as string`() {
        checkTypeStoredValue(ExtraType.Int(), ExtraValue.IntValue(42), "42")
    }

    @Test
    fun `IntValue toStoredSetting zero`() {
        checkTypeStoredValue(ExtraType.Int(), ExtraValue.IntValue(0), "0")
    }

    @Test
    fun `IntValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.Int(), ExtraValue.StringValue("hello"))
    }

    @Test
    fun `BooleanValue toStoredSetting true`() {
        checkTypeStoredValue(ExtraType.Boolean, ExtraValue.BooleanValue(true), "1")
    }

    @Test
    fun `BooleanValue toStoredSetting false`() {
        checkTypeStoredValue(ExtraType.Boolean, ExtraValue.BooleanValue(false), "0")
    }

    @Test
    fun `BooleanValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.Boolean, ExtraValue.IntValue(0))
    }

    @Test
    fun `UriValue toStoredSetting returns value`() {
        checkTypeStoredValue(ExtraType.Uri, ExtraValue.UriValue("geo:0,0"), "geo:0,0")
    }

    @Test
    fun `UriValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.Uri, ExtraValue.StringValue("hello"))
    }

    @Test
    fun `StringArrayValue toStoredSetting returns comma-separated`() {
        checkTypeStoredValue(ExtraType.StringArray, ExtraValue.StringArrayValue(listOf("a", "b")), "a,b")
    }

    @Test
    fun `StringArrayValue toStoredSetting empty returns empty`() {
        checkTypeStoredValue(ExtraType.StringArray, ExtraValue.StringArrayValue(emptyList()), "")
    }

    @Test
    fun `StringArrayValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.StringArray, ExtraValue.IntValue(0))
    }

    @Test
    fun `UriListValue toStoredSetting returns comma-separated`() {
        checkTypeStoredValue(
            ExtraType.UriList,
            ExtraValue.UriListValue(listOf("geo:0,0", "tel:123")),
            "geo:0,0,tel:123",
        )
    }

    @Test
    fun `UriListValue toStoredSetting empty returns empty`() {
        checkTypeStoredValue(ExtraType.UriList, ExtraValue.UriListValue(emptyList()), "")
    }

    @Test
    fun `UriListValue toStoredSetting with wrong type returns default`() {
        checkTypeStoredValueDefault(ExtraType.UriList, ExtraValue.StringValue("hello"))
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
            expected = "extra1: String",
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
                    newActionExtras = listOf(ExtraSpec("extra1", ExtraType.String)),
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
            expected = "extra1: String\nextra2: Int",
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
                    ExtraSpec("extra1", ExtraType.String),
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
                    ExtraSpec("extra1", ExtraType.String),
                    ExtraSpec("extra2", ExtraType.Int()),
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
                    ExtraSpec("extra1", ExtraType.String),
                ),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "string", "value": ""}}""",
        )
    }

    @Test
    fun `rebuildExtras default is type aware`() {
        assertRebuiltExtras(
            values = emptyMap(),
            actionSpecs = listOf(ExtraSpec("extra1", ExtraType.Int())),
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
                    ExtraSpec("extra1", ExtraType.String),
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
                    ExtraSpec("extra1", ExtraType.String),
                ),
            customSpecs =
                listOf(
                    ExtraSpec("extra1", ExtraType.Int()),
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
                    ExtraSpec("extra1", ExtraType.Int()),
                ),
            customSpecs = emptyList(),
            expected = """{"extra1": {"type": "int", "value": 0}}""",
        )
    }

    // ---------- SelectableOption.bitmask ----------

    @Test
    fun `SelectableOption bitmask when int`() {
        assertEquals(SelectableOption("3", 0).bitmask, 3)
    }

    @Test
    fun `SelectableOption bitmask when not int is zero`() {
        assertEquals(SelectableOption("not int", 0).bitmask, 0)
    }

    // ---------- FlagSerialization.BitMask.fromString ----------

    private fun checkFromBitMask(
        options: List<SelectableOption>,
        bitmask: Int,
        expected: List<String>,
    ) = assertEquals(expected, FlagSerialization.BitMask.fromString(bitmask.toString(), options))

    @Test
    fun `toSelectedValues with single option chosen`() {
        checkFromBitMask(
            options = listOf(SelectableOption("2", 0)),
            bitmask = 2,
            expected = listOf("2"),
        )
    }

    @Test
    fun `toSelectedValues with no matching flag returns empty`() {
        checkFromBitMask(
            options = listOf(SelectableOption("2", 0)),
            bitmask = 1,
            expected = emptyList(),
        )
    }

    @Test
    fun `toSelectedValues with two options matches second`() {
        checkFromBitMask(
            options = listOf(SelectableOption("1", 0), SelectableOption("2", 0)),
            bitmask = 2,
            expected = listOf("2"),
        )
    }

    @Test
    fun `toSelectedValues with two matching flags returns both`() {
        checkFromBitMask(
            options = listOf(SelectableOption("1", 0), SelectableOption("2", 0)),
            bitmask = 3,
            expected = listOf("1", "2"),
        )
    }

    // ---------- FlagSerialization.BitMask.toString ----------

    private fun checkToBitmask(
        options: List<SelectableOption>,
        values: List<String>,
        expected: String,
    ) = assertEquals(expected, FlagSerialization.BitMask.toString(values, options))

    @Test
    fun `toBitmask with single value returns its bitmask`() {
        checkToBitmask(
            options = listOf(SelectableOption("2", 0)),
            values = listOf("2"),
            expected = "2",
        )
    }

    @Test
    fun `toBitmask with single value not matching`() {
        checkToBitmask(
            options = listOf(SelectableOption("1", 0)),
            values = listOf("2"),
            expected = "0",
        )
    }

    @Test
    fun `toBitmask with many values choose the proper`() {
        checkToBitmask(
            options = listOf(SelectableOption("2", 0)),
            values = listOf("1", "2"),
            expected = "2",
        )
    }

    @Test
    fun `toBitmask with many options choose the proper`() {
        checkToBitmask(
            options = listOf(SelectableOption("1", 0), SelectableOption("2", 0)),
            values = listOf("2"),
            expected = "2",
        )
    }

    @Test
    fun `toBitmask multiple matching values ored`() {
        checkToBitmask(
            options = listOf(SelectableOption("1", 0), SelectableOption("2", 0)),
            values = listOf("1", "2"),
            expected = "3",
        )
    }

    // ---------- FlagSerialization.CommaSeparated ----------

    private val defaultOptions =
        listOf(
            SelectableOption("option1", 0),
            SelectableOption("option2", 0),
        )

    private fun checkToCommaSeparated(
        values: List<String>,
        expected: String,
        options: List<SelectableOption> = defaultOptions,
    ) = assertEquals(expected, FlagSerialization.CommaSeparated.toString(values, options))

    private fun checkFromCommaSeparated(
        input: String,
        expected: List<String>,
        options: List<SelectableOption> = defaultOptions,
    ) = assertEquals(expected, FlagSerialization.CommaSeparated.fromString(input, options))

    @Test
    fun `CommaSeparated toString with none returns empty`() {
        checkToCommaSeparated(values = emptyList(), expected = "")
    }

    @Test
    fun `CommaSeparated toString with single value`() {
        checkToCommaSeparated(values = listOf("option1"), expected = "option1")
    }

    @Test
    fun `CommaSeparated toString with multiple values`() {
        checkToCommaSeparated(values = listOf("option1", "option2"), expected = "option1,option2")
    }

    @Test
    fun `CommaSeparated fromString with empty returns empty`() {
        checkFromCommaSeparated(input = "", expected = emptyList())
    }

    @Test
    fun `CommaSeparated fromString with single value`() {
        checkFromCommaSeparated(input = "option1", expected = listOf("option1"))
    }

    @Test
    fun `CommaSeparated fromString with multiple values`() {
        checkFromCommaSeparated(input = "option1,option2", expected = listOf("option1", "option2"))
    }

    @Test
    fun `CommaSeparated toString does not filter values not in options (intentional)`() {
        checkToCommaSeparated(values = listOf("unknown"), expected = "unknown")
    }

    @Test
    fun `CommaSeparated fromString does not filter values not in options (intentional)`() {
        checkFromCommaSeparated(input = "unknown", expected = listOf("unknown"))
    }
}
