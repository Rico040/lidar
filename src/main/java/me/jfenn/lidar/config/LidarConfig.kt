package me.jfenn.lidar.config

import kotlinx.serialization.Serializable

@Serializable
class LidarConfig : Config<LidarConfig>() {
    // default block color, if not specified in blockColorMap
    val blockColorDefault: String = "#FFFFFF"

    // map of block id -> particle color code
    val blockColorMap: Map<String, String?> = mapOf(
        "minecraft:air" to null,
        "minecraft:water" to "#0000FF"
    )

    // default entity color, if not specified in entityColorMap
    val entityColorDefault: String = "#FF0000"

    // map of entity id -> particle color code
    val entityColorMap: Map<String, String?> = mapOf(

    )
}