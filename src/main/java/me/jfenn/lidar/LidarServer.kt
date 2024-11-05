package me.jfenn.lidar

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

object LidarServer : DedicatedServerModInitializer {

    override fun onInitializeServer() {
        println("$MOD_ID mod initialized (server)")

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId() = Identifier.of("$MOD_ID:$MOD_ID")
            override fun reload(manager: ResourceManager) {
                Lidar.reload()
            }
        })
    }
}
