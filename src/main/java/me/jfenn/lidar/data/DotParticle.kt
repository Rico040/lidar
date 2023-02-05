package me.jfenn.lidar.data

import me.jfenn.lidar.MOD_ID
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

class DotParticle(
    val clientWorld: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    colorTmp: Double,
    entityIdTmp: Double,
    entityOffsetTmp: Double,
) : AbstractSlowingParticle(clientWorld, x, y, z, 0.0, 0.0, 0.0) {

    private val color = colorTmp.toInt()
    private val entityId = entityIdTmp.takeIf { entityIdTmp != Double.MAX_VALUE }?.toInt()
    private val entityOffset = entityOffsetTmp.toInt().let {
        Vec3d(
            (128.0 - (it shr 16 and 0xFF)) / 32.0,
            (128.0 - (it shr 8 and 0xFF)) / 32.0,
            (128.0 - (it and 0xFF)) / 32.0,
        )
    }

    private val blockPos = BlockPos(x, y, z)

    init {
        // set max age = 1 minute in ticks
        maxAge = 60 * 20
        // set scale = 0.2
        scale = 0.01f
        // set color based on provided color hex int
        red = (color shr 16 and 0xFF) / 255f
        green = (color shr 8 and 0xFF) / 255f
        blue = (color and 0xFF) / 255f
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        if (entityId != null) {
            val entity = clientWorld.getEntityById(entityId)
            if (entity == null || entity.isRemoved) {
                markDead()
                return
            }

            // Calculate a new particle position from the original entityOffset
            val newPos = entity.pos.add(entityOffset);
            x = newPos.x
            y = newPos.y
            z = newPos.z
        } else {
            val block = clientWorld.getBlockState(blockPos)
            if (block.isAir) {
                markDead()
            }
        }
    }

    override fun getSize(tickDelta: Float): Float {
        val f = (age.toFloat() + tickDelta) / maxAge.toFloat()
        return scale * (1.0f - f * f * 0.5f)
    }

    override fun getBrightness(tint: Float): Int {
        val f = ((age.toFloat() + tint) / maxAge.toFloat()).coerceIn(0f, 1f)
        val i = 15728880
        val j = ((i and 255) + (f * 15.0f * 16.0f).toInt()).coerceAtMost(240)
        val k = i shr 16 and 255
        return j or (k shl 16)
    }

    class Factory(private val spriteProvider: SpriteProvider) : ParticleFactory<DefaultParticleType?> {
        override fun createParticle(
            defaultParticleType: DefaultParticleType?,
            clientWorld: ClientWorld,
            d: Double,
            e: Double,
            f: Double,
            g: Double,
            h: Double,
            i: Double
        ): Particle {
            val flameParticle = DotParticle(clientWorld, d, e, f, g, h, i)
            flameParticle.setSprite(spriteProvider)
            return flameParticle
        }
    }

    companion object {
        val DOT = FabricParticleTypes.simple()!!
        val ID = Identifier(MOD_ID, "dot")

        fun register() = Registry.register(Registry.PARTICLE_TYPE, ID, DOT)

        fun registerClient() {
            ParticleFactoryRegistry.getInstance().register(DOT) { it -> Factory(it) }
        }
    }
}
