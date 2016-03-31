package com.lyeeedar.Util

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.Enums

/**
 * Created by Philip on 20-Mar-16.
 */

open class Point constructor(@JvmField var x: Int = 0, @JvmField var y: Int = 0) : Pool.Poolable
{
    constructor( other: Point ) : this(other.x, other.y)

    companion object
    {
		@JvmField val ZERO = Point(0, 0)
		@JvmField val ONE = Point(1, 1)
		@JvmField val MINUS_ONE = Point(-1, -1)

        @JvmField val pool: Pool<Point> = Pools.get( Point::class.java, Int.MAX_VALUE )

        @JvmStatic fun obtain() = Point.pool.obtain()

		@JvmStatic fun freeAll(items: Iterable<Point>) = { for (item in items) item.free() }
    }

    private var obtained = false

    fun set(x: Int, y: Int): Point
    {
        if (obtained) throw RuntimeException()
        obtained = true

        this.x = x
        this.y = y
        return this
    }

    fun set(other: Point) = set(other.x, other.y)

    fun copy() = Point.obtain().set(this);

    fun free() = Point.pool.free(this)

	fun taxiDist(other: Point) = Math.max( Math.abs(other.x - x), Math.abs(other.y - y) )
	fun dist(other: Point) = Math.abs(other.x - x) + Math.abs(other.y - y)
	fun euclideanDist(other: Point) = Vector2.dst(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	fun euclideanDist(ox: Float, oy:Float) = Vector2.dst(x.toFloat(), y.toFloat(), ox, oy)
	fun euclideanDist2(other: Point) = Vector2.dst2(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	fun euclideanDist2(ox: Float, oy:Float) = Vector2.dst2(x.toFloat(), y.toFloat(), ox, oy)

	//operator fun times(other: Int) = obtain().set(x * other, y * other)

	operator fun plus(other: Enums.Direction) = obtain().set(x + other.x, y + other.y)
	operator fun plus(other: Point) = obtain().set(x + other.x, y + other.y)
	operator fun minus(other: Point) = obtain().set(x - other.x, y - other.y)
	operator fun times(other: Point) = obtain().set(x * other.x, y * other.y)
	operator fun div(other: Point) = obtain().set(x / other.x, y / other.y)

	operator fun timesAssign(other: Int) { x *= other; y *= other; }

	operator fun plusAssign(other: Point) { x += other.x; y += other.y }
	operator fun minusAssign(other: Point) { x -= other.x; y -= other.y }
	operator fun timesAssign(other: Point) { x *= other.x; y *= other.y }
	operator fun divAssign(other: Point) { x /= other.x; y /= other.y }

	override fun equals(other: Any?): Boolean
	{
		if (other == null || other !is Point) return false

		return other.x == x && other.y == y
	}

    override fun reset()
    {
        if (!obtained) throw RuntimeException()
        obtained = false

        x = 0
        y = 0
    }
}
