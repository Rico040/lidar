package me.jfenn.lidar.config

import kotlinx.serialization.Serializable

@Serializable
data class LidarConfig(
    val isActive: Boolean = true,

    // default block color, if not specified in blockColorMap
    val blockColorDefault: String = "#FFFFFF",

    // map of block id -> particle color code
    val blockColorMap: Map<String, String?> = mapOf(
        "default" to "#FFFFFF",
        "minecraft:water" to "#0000FF",
        "minecraft:seagrass" to "#0000FF",
        "minecraft:kelp_plant" to "#0000FF",
        "minecraft:lava" to "#ff601c",
    ),

    // map of entity id -> particle color code
    val entityColorMap: Map<String, String?> = mapOf(
        "default" to "#ff7e7e",
        "peaceful" to "#61cf66",
        "hostile" to "#ff7e7e",
        "minecraft:enderman" to "#7e009e",
        "minecraft:creeper" to "#008f07",
    ),

    // block distance of raycast performed to project lidar particles
    val lidarDistance: Double = 10.0,
    // degrees/radius random spread of raycast projections
    val lidarSpread: Float = 30f,
    // amount of lidar projections created per entity, per tick
    val lidarCount: Int = 100,
    // amount of ticks that lidar particles should stay on the screen
    val lidarDuration: Int = 100,
) : Config<LidarConfig>()
