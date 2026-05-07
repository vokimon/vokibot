package net.canvoki.vokibot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

/**
 * Registry for entity types.
 */
class EntityRegistry {
    private val typeInfos = mutableMapOf<String, EntityTypeInfo>()
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    fun register(typeInfo: EntityTypeInfo) {
        typeInfos[typeInfo.typeKey] = typeInfo
    }

    fun getRegisteredTypes(baseClass: KClass<out StorableEntity> = StorableEntity::class): List<EntityTypeInfo> =
        typeInfos.values.filter { baseClass.java.isAssignableFrom(it.entityClass.java) }

    fun <T : StorableEntity> fromJson(jsonString: String, clazz: KClass<T>): T? {
        val jsonObject = json.parseToJsonElement(jsonString) as? JsonObject ?: return null
        val type = jsonObject["type"]?.jsonPrimitive?.content ?: return null
        val typeInfo = typeInfos[type] ?: return null
        val result = typeInfo.deserializer?.invoke(jsonString) ?: return null
        return clazz.safeCast(result)
    }

    fun extractType(jsonString: String): String? {
        val jsonObject = json.parseToJsonElement(jsonString) as? JsonObject ?: return null
        return jsonObject["type"]?.jsonPrimitive?.content
    }

    private fun <T : StorableEntity> KClass<T>.safeCast(entity: StorableEntity): T? =
        if (this.isInstance(entity)) this.java.cast(entity) else null
}
