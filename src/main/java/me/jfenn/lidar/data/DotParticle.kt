package me.jfenn.lidar.data

import me.jfenn.lidar.Lidar.config
import me.jfenn.lidar.MOD_ID
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.client.particle.*
import net.minecraft.client.world.ClientWorld
import net.minecraft.particle.DefaultParticleType
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import java.lang.ref.WeakReference

class DotParticle(
    clientWorld: ClientWorld,
    x: Double,
    y: Double,
    z: Double,
    colorTmp: Double,
    entityIdTmp: Double,
    entityOffsetTmp: Double,
) : AbstractSlowingParticle(clientWorld, x, y, z, 0.0, 0.0, 0.0) {

    private val color = colorTmp.toBits().toInt()

    private val entityId = entityIdTmp.toBits().takeIf { it != 0L }?.let {
        (it ushr 32 and 0xFFFFFFFF).toInt()
    }
    private val entityReference = WeakReference(entityId?.let { clientWorld.getEntityById(it) })
    private val entityOffset = entityOffsetTmp.toBits().takeIf { it != 0L }?.let {
        vec3dFromBits(it)
    }

    private val blockPos = BlockPos(x, y, z)
    private val blockIdentity = run {
        world.getBlockState(blockPos)
    }.let {
        System.identityHashCode(it)
    }

    init {
        // set max age from config
        maxAge = if (entityId != null) config.lidarDurationEntity else config.lidarDurationBlock
        // set scale = 0.2
        scale = 0.01f
        // set color based on provided color hex int
        red = (color shr 16 and 0xFF) / 255f
        green = (color shr 8 and 0xFF) / 255f
        blue = (color and 0xFF) / 255f
    }

    override fun getType(): ParticleTextureSheet = ParticleTextureSheet.PARTICLE_SHEET_OPAQUE

    override fun move(dx: Double, dy: Double, dz: Double) {
        if (entityId == null) {
            // if the particle is a block, don't do any transforms...
            val block = world.getBlockState(blockPos)
            if (block.isAir || System.identityHashCode(block) != blockIdentity) markDead()
            return
        }

        if (!config.entityParticleFollow) {
            // Don't add any behavior for entity IDs
            return
        }

        val entity = entityReference.get()
        if (entity == null || entity.isRemoved) {
            // entity has been killed, or is out of view
            markDead()
            return
        }

        // Calculate a new particle position from the original entityOffset
        if (entityOffset != null) {
            val newPos = entityOffset.rotateY(entity.bodyYaw)
                .add(entity.pos)

            x = newPos.x
            y = newPos.y
            z = newPos.z
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

        private const val VEC3D_SCALE = 128.0
        private const val VEC3D_MIN = -4096L
        private const val VEC3D_MAX = 4096L

        fun Vec3d.toBits(): Long {
            return (((x * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN) shl 42)
                .or(((y * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN) shl 21)
                .or((z * VEC3D_SCALE).toLong().coerceIn(VEC3D_MIN, VEC3D_MAX) - VEC3D_MIN)
        }

        fun vec3dFromBits(bits: Long): Vec3d {
            return Vec3d(
                ((bits ushr 42 and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
                ((bits ushr 21 and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
                ((bits and 0xFFFF) + VEC3D_MIN) / VEC3D_SCALE,
            )
        }
    }
}
