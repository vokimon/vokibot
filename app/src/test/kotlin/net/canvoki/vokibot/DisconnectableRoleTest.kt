package net.canvoki.vokibot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import net.canvoki.shared.test.assertEquals
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
            HEADSET: Calls
            A2DP_SINK: Multimedia speaker
            HID_HOST: Keyboard & mouse
            PAN: Network sharing
            HEADSET_CLIENT: Speakerphone
            """.trimIndent(),
            dumpRoles(),
        )
    }

    @Test @Config(qualifiers = "ca")
    fun `catalan labels`() {
        assertEquals(
            """
            A2DP: Font multimèdia
            HEADSET: Trucades
            A2DP_SINK: Altaveu multimèdia
            HID_HOST: Teclat i ratolí
            PAN: Compartir xarxa
            HEADSET_CLIENT: Altaveu de trucada
            """.trimIndent(),
            dumpRoles(),
        )
    }

    @Test
    fun `profile ids`() {
        assertEquals(
            """
            A2DP: 2
            HEADSET: 1
            A2DP_SINK: 11
            HID_HOST: 4
            PAN: 5
            HEADSET_CLIENT: 16
            """.trimIndent(),
            dumpRoleIds(),
        )
    }
}
