package net.canvoki.vokibot

import android.content.Context
import io.mockk.mockk
import net.canvoki.shared.test.assertEquals
import net.canvoki.shared.test.assertJsonEqual
import net.canvoki.vokibot.nfc.NfcTrigger
import org.junit.Assert.assertTrue
import org.junit.Test

class NfcTriggerTest {
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
    fun `toJson`() {
        assertJsonEqual(nfcTriggerBase().toJson(), nfcTriggerJson())
    }

    @Test
    fun `fromJson`() {
        val deserialized = NfcTrigger.fromJson(nfcTriggerJson())
        assertEquals(nfcTriggerBase().toString(), deserialized.toString())
    }

    @Test
    fun `id`() {
        val nfc = nfcTriggerBase()
        assertEquals(nfc.id, "nfc_01_23_45_67_AB_CD_EF")
    }

    @Test
    fun `description returns uid`() {
        val nfc = nfcTriggerBase()
        assertEquals("01:23:45:67:AB:CD:EF", nfc.description(mockk<Context>()))
    }
}
