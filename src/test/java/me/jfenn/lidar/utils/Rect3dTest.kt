package me.jfenn.lidar.utils

import net.minecraft.util.math.Vec3d
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Rect3dTest {

    @Test
    fun `a ray (0,1,0) (0,-1,0) should intersect with the center of a flat plane at 0,0`() {
        val origin = Vec3d(0.0, 1.0, 0.0)
        val direction = Vec3d(0.0, -1.0, 0.0)

        val rect = Rect3d(
            Vec3d(1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, -1.0),
            Vec3d(1.0, 0.0, -1.0),
        )

        val expected = Vec3d(0.0, 0.0, 0.0)
        val actual = rect.findIntersection(origin, direction)

        assertEquals(expected, actual)
    }

    @Test
    fun `a ray (0,-1,0) (0,-1,0) should not intersect with a flat plane at 0,0`() {
        val origin = Vec3d(0.0, -1.0, 0.0)
        val direction = Vec3d(0.0, -1.0, 0.0)

        val rect = Rect3d(
            Vec3d(1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, -1.0),
            Vec3d(1.0, 0.0, -1.0),
        )

        val actual = rect.findIntersection(origin, direction)

        assertNull(actual)
    }

    @Test
    fun `a ray (5,1,5) (-5,-1,-5) should intersect with the center of a flat plane at 0,0`() {
        val origin = Vec3d(5.0, 1.0, 5.0)
        val direction = Vec3d(-5.0, -1.0, -5.0).normalize()

        val rect = Rect3d(
            Vec3d(1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, -1.0),
            Vec3d(1.0, 0.0, -1.0),
        )

        val expected = Vec3d(0.0, 0.0, 0.0)
        val actual = rect.findIntersection(origin, direction)

        assertEquals(expected, actual)
    }

    @Test
    fun `a ray (3,1,3) (0,-1,0) should not intersect a flat plane at (0,0) outside of the plane's boundary`() {
        val origin = Vec3d(3.0, 1.0, 3.0)
        val direction = Vec3d(0.0, -1.0, 0.0)

        val rect = Rect3d(
            Vec3d(1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, 1.0),
            Vec3d(-1.0, 0.0, -1.0),
            Vec3d(1.0, 0.0, -1.0),
        )

        val actual = rect.findIntersection(origin, direction)

        assertNull(actual)
    }

    @Test
    fun `a ray (-2167, 75, 2544) (-5, -1, -5) should intersect a diagonal plane`() {
        val origin = Vec3d(-2167.0, 75.0, 2544.0)
        val direction = Vec3d(-5.0, -1.0, -5.0)

        val rect = Rect3d(
            Vec3d(-2177.0, 75.0, 2544.0),
            Vec3d(-2167.0, 75.0, 2534.0),
            Vec3d(-2177.0, 71.0, 2544.0),
            Vec3d(-2167.0, 71.0, 2534.0),
        )

        val expected = Vec3d(-2172.0, 74.0, 2539.0)
        val actual = rect.findIntersection(origin, direction)

        assertEquals(expected, actual)
    }

}