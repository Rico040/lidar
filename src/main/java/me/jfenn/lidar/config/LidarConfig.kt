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
        "minecraft:lava" to "#ff601c",
    )

    // default entity color, if not specified in entityColorMap
    val entityColorDefault: String = "#ff7e7e"
    val entityColorPeaceful: String = "#61cf66"
    val entityColorHostile = "#ff7e7e"

    // map of entity id -> particle color code
    val entityColorMap: Map<String, String?> = mapOf(
        "minecraft:enderman" to "#7e009e",
        "minecraft:creeper" to "#008f07",
    )

    // block distance of raycast performed to project lidar particles
    val lidarDistance: Double = 10.0
    // degrees/radius random spread of raycast projections
    val lidarSpread: Float = 30f
    // amount of lidar projections created per entity, per tick
    val lidarCount: Int = 100
    // amount of ticks that lidar particles should stay on the screen
    val lidarDuration: Int = 100
}