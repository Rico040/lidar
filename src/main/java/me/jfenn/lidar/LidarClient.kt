package me.jfenn.lidar

import me.jfenn.lidar.data.DotParticle
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
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
            client.crosshairTarget?.let {
                Lidar.particles.addHit(client, it)
            }

            Lidar.particles.tick()
            // TODO: add same behavior for other rendered players - client.world?.players?.forEach { player -> }
        })

        DotParticle.registerClient()
    }

}
