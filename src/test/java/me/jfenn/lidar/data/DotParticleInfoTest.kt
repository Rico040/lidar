package me.jfenn.lidar.data

import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals

class DotParticleInfoTest {

    @Test
    fun `dot particle info encodes and decodes the same(ish) entityOffset value`() {
        val expected = Vec3d(-0.5, 0.6, 0.7)
        val expectedInfo = DotParticle.Info(0, entityId = 1, entityOffset = expected)

        val (i, j, k) = expectedInfo.encode()
        val actualInfo = DotParticle.Info.decode(i, j, k)
        val actual = actualInfo.entityOffset!!

        assert(abs(expected.x - actual.x) < (1.0/32)) { "expected $expected but was $actual" }
        assert(abs(expected.y - actual.y) < (1.0/32)) { "expected $expected but was $actual" }
        assert(abs(expected.z - actual.z) < (1.0/32)) { "expected $expected but was $actual" }
    }

    @Test
    fun `dot particle info encodes and decodes the same entityId value`() {
        val expected = 42
        val expectedInfo = DotParticle.Info(0, entityId = expected)

        val (i, j, k) = expectedInfo.encode()
        val actualInfo = DotParticle.Info.decode(i, j, k)
        val actual = actualInfo.entityId

        assertEquals(expected, actual, "expected $expected, encoded as ($i, $j, $k) -> $actual")
    }

}