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
}
