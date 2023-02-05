package me.jfenn.lidar

import me.jfenn.lidar.config.LidarConfig
import me.jfenn.lidar.config.config
import me.jfenn.lidar.data.DotParticle
import me.jfenn.lidar.services.ParticleService
import me.jfenn.lidar.utils.EventListener
import net.fabricmc.api.ModInitializer

const val MOD_ID = "lidar"

val onReload = EventListener<Unit>()

object Lidar : ModInitializer {

    val config: LidarConfig by config()
    val particles: ParticleService by lazy { ParticleService() }

    override fun onInitialize() {
        println("$MOD_ID mod initialized (main)")
        DotParticle.register()
    }

    fun reload() {
        onReload(Unit)
    }

}
