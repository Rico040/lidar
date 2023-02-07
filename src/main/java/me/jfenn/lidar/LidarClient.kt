package me.jfenn.lidar

import me.jfenn.lidar.Lidar.config
import me.jfenn.lidar.data.DotParticle
import me.jfenn.lidar.services.EntityModelService
import me.jfenn.lidar.services.ParticleService
import me.jfenn.lidar.services.RayCastService
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW

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
            if (!config.isActive) return@StartTick
            if (client.isPaused) return@StartTick
            val world = client.world ?: return@StartTick
            val playerPos = client.player?.pos ?: return@StartTick

            val onlyPlayers = false

            for (entity in world.entities) {
                // skip entities outside of render distance
                if (entity !is LivingEntity || entity.isRemoved) continue
                if (!config.entityParticles && !entity.isPlayer) continue
                if (!entity.shouldRender(playerPos.distanceTo(entity.pos))) continue

                // if the entity is in a block, only render one particle instead of casting projection
                // (saves particle limit against fish/etc)
                if ((!entity.isPlayer && entity.isSubmergedInWater) || entity.isInsideWall) {
                    val color = world.getBlockState(entity.blockPos)?.let {
                        ParticleService.getBlockColor(it)
                    } ?: continue

                    ParticleService.addParticle(world, entity.eyePos, DotParticle.Info(color))
                    continue
                }

                val projections = RayCastService.getEntityProjections(entity, config.lidarSpread, config.lidarCount)
                for (projection in projections) {
                    val (blockHit, entityHit) = RayCastService.raycastInDirection(entity, entity.eyePos, projection)

                    // if entityHit is the current player, don't render the particle
                    if (!config.entityParticlesOnSelf && entityHit?.entity?.id == client.player?.id)
                        continue

                    entityHit?.let { hit ->
                        EntityModelService.getCollisionPoint(hit.entity, hit.pos, projection)
                    }?.also { pos ->
                        ParticleService.addEntityHit(entityHit, pos)
                    } ?: blockHit?.let { hit ->
                        ParticleService.addBlockHit(hit)
                    }
                }
            }
        })

        // TODO: on block update, invalidate particles at block

        // TODO: raycast behind water, ignoring fluids

        DotParticle.registerClient()

        // when "active" key pressed, toggle isActive config
        val keyActive = KeyBindingHelper.registerKeyBinding(KeyBinding(
            "key.lidar.active",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.lidar.category"
        ))

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            var isActive = config.isActive
            while (keyActive.wasPressed()) isActive = !isActive

            if (isActive != config.isActive) {
                config = config.copy(isActive = isActive)
            }
        })
    }

}
