package me.jfenn.lidar

import me.jfenn.lidar.data.DotParticle
import me.jfenn.lidar.services.ParticleService
import me.jfenn.lidar.services.RayCastService
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.entity.LivingEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object LidarClient : ClientModInitializer {

    override fun onInitializeClient() {
        println("$MOD_ID mod initialized (client)")

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId() = Identifier("$MOD_ID:$MOD_ID")
            override fun reload(manager: ResourceManager) {
                Lidar.reload()
            }
        })

        ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client ->
            if (client.isPaused) return@StartTick
            val world = client.world ?: return@StartTick
            val playerPos = client.player?.pos ?: return@StartTick

            for (entity in world.entities) {
                // skip entities outside of render distance
                if (!entity.shouldRender(playerPos.distanceTo(entity.pos))) continue
                if (entity !is LivingEntity || entity.isRemoved) continue

                // TODO: config setting to render only player particles
                val projections = RayCastService.getEntityProjections(entity, Lidar.config.lidarSpread, Lidar.config.lidarCount)
                for (projection in projections) {
                    val hit = RayCastService.raycastInDirection(entity, entity.eyePos, projection) ?: continue
                    ParticleService.addHit(client, hit)
                }
            }
        })

        DotParticle.registerClient()
    }

}
