package me.jfenn.lidar.config

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import me.jfenn.lidar.MOD_ID
import me.jfenn.lidar.onReload
import me.jfenn.lidar.utils.EventListener
import me.jfenn.lidar.utils.json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

abstract class Config<S: Any> {

    abstract val schemaVersion: Int

    val schema: Int = 0

    @Transient
    val onChange = EventListener<S>()

    companion object {
        fun path(clazz: KClass<*>) : Path {
            return FabricLoader.getInstance().configDir.resolve("${MOD_ID}-${clazz.java.simpleName.lowercase()}.json")
        }

        @OptIn(InternalSerializationApi::class)
        fun <T: Config<T>> read(clazz: KClass<T>) : T {
            val config: T = try {
                json.decodeFromStream(clazz.serializer(), Files.newInputStream(path(clazz)))
            } catch (e: Exception) {
                e.printStackTrace()
                json.decodeFromString(clazz.serializer(), "{}").also { write(clazz, it) }
            }

            // if the schema doesn't match, overwrite with new config
            return if (config.schema != config.schemaVersion)
                json.decodeFromString(clazz.serializer(), "{}").also { write(clazz, it) }
            else config
        }

        @OptIn(InternalSerializationApi::class)
        fun <T: Config<T>> write(clazz: KClass<T>, config: T) {
            val path = path(clazz)
            if (!Files.exists(path)) Files.createFile(path)
            Files.write(path, json.encodeToString(clazz.serializer(), config).toByteArray())

            config.onChange(config)
        }
    }
}

inline fun <reified T: Config<T>> T.write() = Config.write(T::class, this)

class ConfigDelegate<T: Config<T>>(
    val clazz: KClass<T>
) : ReadWriteProperty<Any, T> {
    var value: T? = null

    init {
        onReload {
            // reset stored value on any reload event
            value = null
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value ?: Config.read(clazz).also { value = it }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value.also { Config.write(clazz, value) }
    }
}

inline fun <reified T: Config<T>> config() = ConfigDelegate(T::class)
