package me.jfenn.lidar

import me.jfenn.lidar.Lidar.config
import me.jfenn.lidar.data.DotParticle
import me.jfenn.lidar.services.EntityModelService
import me.jfenn.lidar.services.MusicService
import me.jfenn.lidar.services.ParticleService
import me.jfenn.lidar.services.RayCastService
import me.jfenn.lidar.utils.tryOrNull
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import org.ladysnake.satin.api.event.PostWorldRenderCallback
import org.ladysnake.satin.api.managed.ShaderEffectManager
import org.lwjgl.glfw.GLFW

object LidarClient : ClientModInitializer {

    private val shader = ShaderEffectManager.getInstance().manage(Identifier.of(MOD_ID, "shaders/post/particles.json"))

    override fun onInitializeClient() {
        println("$MOD_ID mod initialized (client)")

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun getFabricId() = Identifier.of("$MOD_ID:$MOD_ID")
            override fun reload(manager: ResourceManager) {
                Lidar.reload()
            }
        })

        ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client ->
            if (!config.isActive) return@StartTick
            if (client.isPaused) return@StartTick
            val world = client.world ?: return@StartTick
            val playerPos = client.player?.pos ?: return@StartTick

            val entities = world.entities.asSequence().filter { entity ->
                // skip entities outside of render distance
                if (entity !is LivingEntity || entity.isRemoved) return@filter false
                if (!entity.shouldRender(playerPos.distanceTo(entity.pos))) return@filter false
                if (entity.isSpectator) return@filter false

                // don't attempt collisions on excluded entity types
                val entityType = Registries.ENTITY_TYPE.getId(entity.type).toString()
                if (config.entityRender.contains(entityType)) return@filter false

                // if the entity is in a block, only render one particle instead of casting projection
                // (saves particle limit against fish/etc)
                if ((!entity.isPlayer && entity.isSubmergedInWater) || entity.isInsideWall) {
                    val color = world.getBlockState(entity.blockPos)?.let {
                        ParticleService.getBlockColor(it)
                    } ?: return@filter false

                    ParticleService.addParticle(world, entity.eyePos, color)
                    return@filter false
                }

                true
            }

            val projectionsByEntity = entities.flatMap { entity ->
                val count = if (entity.isPlayer) config.lidarCount else config.entityLidarCount
                RayCastService.getEntityProjections(entity, config.lidarSpread, count)
            }.groupBy { projection ->
                projection.entityHit?.entity?.id
            }

            for ((entityId, projections) in projectionsByEntity) {
                // if entityHit is the current player, don't render the particle
                if (!config.entityParticlesOnSelf && entityId == client.player?.id)
                    continue

                entityId?.let {
                    world.getEntityById(entityId)
                }?.takeIf {
                    // don't attempt raycasts on excluded entity types
                    val entityHitType = Registries.ENTITY_TYPE.getId(it.type).toString()
                    !config.entityRender.contains(entityHitType) && !it.isSpectator
                }?.let {
                    // if entities should project onto the entity model...
                    if (config.entityParticleModel) {
                        // attempt to render the entity model
                        tryOrNull {
                            EntityModelService.getCollisionPoints(it, projections)
                        }
                    } else {
                        // use the hitbox location as an entity intersection
                        for (projection in projections) {
                            projection.entityHitPos = projection.entityHit?.pos
                        }
                    }
                }

                // create the projected particles
                for (projection in projections) {
                    projection.entityHit?.let { entityHit ->
                        projection.entityHitPos?.let { pos ->
                            ParticleService.addEntityHit(entityHit, pos)
                        }
                    } ?: projection.blockHit?.let {
                        ParticleService.addBlockHit(it)
                    }
                }
            }
        })

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

            MusicService.tick()
        })

        PostWorldRenderCallback.EVENT.register { _, tickDelta ->
            if (config.isActive && config.lidarBloom)
                shader.render(tickDelta)
        }
    }

}
