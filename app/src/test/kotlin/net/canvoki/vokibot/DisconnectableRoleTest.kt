package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import net.canvoki.shared.test.assertEquals
import net.canvoki.vokibot.bluetooth.DisconnectableRole
import net.canvoki.vokibot.bluetooth.getLabel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en")
class DisconnectableRoleTest {
    private fun context(): Context = ApplicationProvider.getApplicationContext()

    private fun dumpRoles(): String =
        DisconnectableRole.entries.joinToString("\n") { role ->
            "${role.name}: ${role.getLabel(context())}"
        }

    private fun dumpRoleIds(): String =
        DisconnectableRole.entries.joinToString("\n") { role ->
            "${role.name}: ${role.profileId}"
        }

    @Test
    fun `english labels`() {
        assertEquals(
            """
            A2DP: Multimedia source
            A2DP_SINK: Speaker/Headphones
            AVRCP_CONTROLLER: Media controller
            AVRCP_TARGET: Media player
            HEADSET: Calls
            HEADSET_CLIENT: Speakerphone
            HID_DEVICE: Input device
            HID_HOST: Input receiver
            MAP: Messages
            MAP_CLIENT: Message client
            PAN: Shared network
            PBAP: Contacts
            PBAP_CLIENT: Contacts client
            """.trimIndent(),
            dumpRoles(),
        )
    }

    @Test
    @Config(qualifiers = "ca")
    fun `catalan labels`() {
        assertEquals(
            """
            A2DP: Font multimèdia
            A2DP_SINK: Altaveu/Auriculars
            AVRCP_CONTROLLER: Controlador multimèdia
            AVRCP_TARGET: Reproductor multimèdia
            HEADSET: Trucades
            HEADSET_CLIENT: Mans lliures
            HID_DEVICE: Dispositiu d'entrada
            HID_HOST: Receptor d'entrada
            MAP: Missatges
            MAP_CLIENT: Client de missatgeria
            PAN: Xarxa compartida
            PBAP: Contactes
            PBAP_CLIENT: Client de contactes
            """.trimIndent(),
            dumpRoles(),
        )
    }

    @Test
    fun `profile ids`() {
        assertEquals(
            """
            A2DP: 2
            A2DP_SINK: 11
            AVRCP_CONTROLLER: 12
            AVRCP_TARGET: 13
            HEADSET: 1
            HEADSET_CLIENT: 16
            HID_DEVICE: 19
            HID_HOST: 4
            MAP: 9
            MAP_CLIENT: 18
            PAN: 5
            PBAP: 6
            PBAP_CLIENT: 17
            """.trimIndent(),
            dumpRoleIds(),
        )
    }
}
