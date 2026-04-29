package net.canvoki.vokibot

import kotlinx.serialization.json.Json
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import org.junit.Assert.assertTrue
import org.junit.Test

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
            uid = "01:23:45:67:AB:CD:EF",
        )

    fun nfcTriggerJson() =
        """
        {
          "type": "trigger_nfc",
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

    @Test
    fun `NfcTrigger id`() {
        val nfc = nfcTriggerBase()
        assertEquals(nfc.id, "01_23_45_67_AB_CD_EF")
    }
}
