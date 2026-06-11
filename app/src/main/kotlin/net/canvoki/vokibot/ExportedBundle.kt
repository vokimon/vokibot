package net.canvoki.vokibot

import android.content.Context
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private val PrettyJson =
    Json(JsonConfig) {
        prettyPrint = true
    }

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

data class ImportAnalysis(
    val overwritten: Set<String>,
    val repositoryReferences: Set<String>,
    val referencedMissing: Set<String>,
) {
    @Suppress("UNUSED_PARAMETER")
    fun summary(context: Context): String =
        buildString {
            if (overwritten.isNotEmpty()) {
                appendLine("Will overwrite:")
                appendLine()
                overwritten.forEach { appendLine(it) }
                appendLine()
            }
            if (repositoryReferences.isNotEmpty()) {
                appendLine("References to existing entities:")
                appendLine()
                repositoryReferences.forEach { appendLine(it) }
                appendLine()
            }
            if (referencedMissing.isNotEmpty()) {
                appendLine("Missing references:")
                appendLine()
                referencedMissing.forEach { appendLine(it) }
                appendLine()
            }
        }
}

@Serializable
data class ExportedBundle(
    @Serializable(with = EntityListSerializer::class)
    val entities: List<StorableEntity>,
    val version: Int = 1,
) {
    fun entityIds(): Set<String> = entities.map { it.id }.toSet()

    fun references(): Set<String> = entities.flatMap { it.references() }.toSet()

    fun analyzeImport(repoEntityIds: Set<String>): ImportAnalysis {
        val bundleIds = entityIds()
        val bundleRefs = references()
        return ImportAnalysis(
            overwritten = bundleIds intersect repoEntityIds,
            repositoryReferences = (bundleRefs - bundleIds) intersect repoEntityIds,
            referencedMissing = (bundleRefs - bundleIds) - repoEntityIds,
        )
    }

    fun toJson(): String = PrettyJson.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String): ExportedBundle = JsonConfig.decodeFromString(ExportedBundle.serializer(), json)
    }
}
