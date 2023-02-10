package me.jfenn.lidar.config

import kotlinx.serialization.Serializable

@Serializable
data class LidarConfig(
    override val schemaVersion: Int = 3,
    val isActive: Boolean = true,

    // map of block id -> particle color code
    val blockColorMap: Map<String, String?> = mapOf(
        "default" to "#FFFFFF",
        "minecraft:water" to "#0000FF",
        "minecraft:seagrass" to "#0000FF",
        "minecraft:kelp" to "#0000FF",
        "minecraft:kelp_plant" to "#0000FF",
        "minecraft:lava" to "#ff601c",
        "minecraft:iron_ore" to "#d6ccb8",
        "minecraft:deepslate_iron_ore" to "#d6ccb8",
        "minecraft:coal_ore" to "#dedede",
        "minecraft:deepslate_coal_ore" to "#dedede",
        "minecraft:redstone_ore" to "#ffe3e3",
        "minecraft:deepslate_redstone_ore" to "#ffe3e3",
        "minecraft:diamond_ore" to "#e3f9fc",
        "minecraft:deepslate_diamond_ore" to "#e3f9fc",
        "minecraft:emerald_ore" to "#deffe2",
        "minecraft:deepslate_emerald_ore" to "#deffe2",
        "minecraft:lapis_ore" to "#e8e9fc",
        "minecraft:deepslate_lapis_ore" to "#e8e9fc",
        "minecraft:copper_ore" to "#faf0eb",
        "minecraft:deepslate_copper_ore" to "#faf0eb",
        "minecraft:gold_ore" to "#fff7d1",
        "minecraft:deepslate_gold_ore" to "#fff7d1",
        "minecraft:nether_quartz_ore" to "#e6e6e6",
        "minecraft:nether_gold_ore" to "#fff7d1",
    ),

    // map of entity id -> particle color code
    val entityColorMap: Map<String, String?> = mapOf(
        "default" to "#ff7e7e",
        "peaceful" to "#61cf66",
        "hostile" to "#ff7e7e",
        "minecraft:enderman" to "#7e009e",
        "minecraft:creeper" to "#008f07",
    ),

    // set of entities that should render normally, ignoring the render mixin
    val entityRender: Set<String> = setOf(
        "minecraft:item",
        "minecraft:item_frame",
        "minecraft:glow_item_frame",
        "minecraft:ender_dragon",
    ),

    // whether particles should follow entities as they move
    val entityParticleFollow: Boolean = false,
    // if enabled, particles follow entity models more accurately, at the cost of performance
    val entityParticleFollowModel: Boolean = false,
    // if enabled, particles from other entities will render on the current player
    val entityParticlesOnSelf: Boolean = false,
    // if enabled, any living entity also renders particles cast from its line of sight
    val entityParticles: Boolean = true,

    // block distance of raycast performed to project lidar particles
    val lidarDistance: Double = 10.0,
    // degrees/radius random spread of raycast projections
    val lidarSpread: Float = 30f,
    // amount of lidar projections created per entity, per tick
    val lidarCount: Int = 100,
    // amount of ticks that lidar particles should stay on the screen
    val lidarDurationBlock: Int = 100,
    // amount of ticks that lidar particles on entities should stay on the screen
    val lidarDurationEntity: Int = 20,
) : Config<LidarConfig>()
