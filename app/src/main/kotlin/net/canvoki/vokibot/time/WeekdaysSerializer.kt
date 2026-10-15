package net.canvoki.vokibot.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import java.time.DayOfWeek

object WeekdaysSerializer : KSerializer<Set<DayOfWeek>> {
    private val delegate = SetSerializer(serializer<DayOfWeek>())

    override val descriptor = delegate.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Set<DayOfWeek>,
    ) = delegate.serialize(encoder, value.sorted().toSet())

    override fun deserialize(decoder: Decoder): Set<DayOfWeek> = delegate.deserialize(decoder)
}
