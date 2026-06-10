package net.canvoki.vokibot

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object EntityListSerializer : KSerializer<List<StorableEntity>> {
    override val descriptor: SerialDescriptor =
        ListSerializer(JsonObject.serializer()).descriptor

    override fun serialize(
        encoder: Encoder,
        value: List<StorableEntity>,
    ) {
        val jsonObjects = value.map { Json.parseToJsonElement(it.toJson()).jsonObject }
        encoder.encodeSerializableValue(ListSerializer(JsonObject.serializer()), jsonObjects)
    }

    override fun deserialize(decoder: Decoder): List<StorableEntity> {
        val jsonObjects =
            decoder.decodeSerializableValue(ListSerializer(JsonObject.serializer()))
        return jsonObjects.map { StorableEntity.fromJson(it.toString()) }
    }
}

@Serializable
data class ExportedBundle(
    @Serializable(with = EntityListSerializer::class)
    val entities: List<StorableEntity>,
    val version: Int = 1,
) {
    fun entityIds(): Set<String> = entities.map { it.id }.toSet()

    fun toJson(): String = JsonConfig.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String): ExportedBundle = JsonConfig.decodeFromString(ExportedBundle.serializer(), json)
    }
}
