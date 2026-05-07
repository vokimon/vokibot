package net.canvoki.vokibot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

val json =
    Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

/**
 * Registry for entity types.
 */
class EntityRegistry {
    private val typeInfos = mutableMapOf<String, EntityTypeInfo>()

    fun register(typeInfo: EntityTypeInfo) {
        typeInfos[typeInfo.typeKey] = typeInfo
    }

    fun getRegisteredTypes(baseClass: KClass<out StorableEntity> = StorableEntity::class): List<EntityTypeInfo> =
        typeInfos.values.filter { baseClass.java.isAssignableFrom(it.entityClass.java) }

    fun getTypeInfo(key: String): EntityTypeInfo? = typeInfos[key]

    inline fun <reified T : StorableEntity> fromJson(jsonString: String): T? {
        val preview = json.decodeFromString<Preview>(jsonString)
        val factory = getTypeInfo(preview.type)?.deserializer
        if (factory == null) {
            log("Unknown trigger type: ${preview.type}")
            return null
        }
        val result = factory(jsonString)
        if (result is T) return result
        log("Expected a Trigger, got: ${preview.type}")
        return null
    }

    @Serializable
    data class Preview(
        val type: String,
    )
}
