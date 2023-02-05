package me.jfenn.lidar.config

import kotlinx.serialization.Serializable

@Serializable
class LidarConfig : Config<LidarConfig>() {
    // default block color, if not specified in blockColorMap
    val blockColorDefault: String = "#FFFFFF"

    // map of block id -> particle color code
    val blockColorMap: Map<String, String?> = mapOf(
        "minecraft:air" to null,
        "minecraft:water" to "#0000FF",
    )

    // default entity color, if not specified in entityColorMap
    val entityColorDefault: String = "#ff7e7e"
    val entityColorPeaceful: String = "#61cf66"
    val entityColorNeutral: String = "#ff601c"
    val entityColorHostile = "#ff7e7e"

    // map of entity id -> particle color code
    val entityColorMap: Map<String, String?> = mapOf(
        "minecraft:enderman" to "#7e009e",
        "minecraft:creeper" to "#008f07",
    )
}