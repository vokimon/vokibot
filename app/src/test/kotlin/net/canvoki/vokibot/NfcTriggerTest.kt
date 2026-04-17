package net.canvoki.vokibot

import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual

class NfcTriggerTest {
    private val json =
        Json {
            explicitNulls = false
            encodeDefaults = true
            classDiscriminator = "type"
        }

    // ---------- LaunchActivityCommand ----------
    fun nfcTriggerBase() =
        NfcTrigger(
            displayName = "My ID Card",
            uid = "01:23:45:67:AB:CD:EF"
        )

    fun nfcTriggerJson() =
        """
        {
          "type": "nfc_trigger",
          "displayName": "My ID Card",
          "uid": "01:23:45:67:AB:CD:EF"
        }
        """.trimIndent()

    @Test
    fun `NfcTrigger toJson`() {
        assertJsonEqual(nfcTriggerBase().toJson(), nfcTriggerJson())
    }

    @Test
    fun `NfcTrigger fromJson`() {
        val deserialized = NfcTrigger.fromJson(nfcTriggerJson())
        assertEquals(nfcTriggerBase().toString(), deserialized.toString())
    }
}
