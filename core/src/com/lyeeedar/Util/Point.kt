package com.lyeeedar.Util

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.Direction

/**
 * Created by Philip on 20-Mar-16.
 */

open class Point : Pool.Poolable
{
	var locked: Boolean = false
	var fromPool: Boolean = false

	var x: Int = 0
		set(value)
		{
			if (locked) throw RuntimeException("Tried to edit a locked point")
			if (fromPool && !obtained) throw RuntimeException("Tried to edit a freed point")
			field = value
		}

	var y: Int = 0
		set(value)
		{
			if (locked) throw RuntimeException("Tried to edit a locked point")
			if (fromPool && !obtained) throw RuntimeException("Tried to edit a freed point")
			field = value
		}

	constructor()
	{

	}

	constructor(x: Int, y: Int)
	{
		this.x = x
		this.y = y
	}
	constructor(x: Int, y: Int, locked: Boolean) : this(x, y)
	{
		this.locked = locked
	}
    constructor( other: Point ) : this(other.x, other.y)

    companion object
    {
		@JvmField val ZERO = Point(0, 0, true)
		@JvmField val ONE = Point(1, 1, true)
		@JvmField val MINUS_ONE = Point(-1, -1, true)
		@JvmField val MAX = Point(Int.MAX_VALUE, Int.MAX_VALUE, true)
		@JvmField val MIN = Point(-Int.MAX_VALUE, -Int.MAX_VALUE, true)

        private val pool: Pool<Point> = Pools.get( Point::class.java, Int.MAX_VALUE )

        @JvmStatic fun obtain(): Point
		{
			val point = Point.pool.obtain()
			point.fromPool = true

			if (point.obtained) throw RuntimeException()

			point.obtained = true
			return point
		}

		@JvmStatic fun freeAll(items: Iterable<Point>) = { for (item in items) item.free() }
    }

    private var obtained = false

    fun set(x: Int, y: Int): Point
    {
        this.x = x
        this.y = y
        return this
    }

    fun set(other: Point) = set(other.x, other.y)

    fun copy() = Point.obtain().set(this);

    fun free() { if (obtained) { Point.pool.free(this); obtained = false } }

	fun taxiDist(other: Point) = Math.max( Math.abs(other.x - x), Math.abs(other.y - y) )
	fun dist(other: Point) = Math.abs(other.x - x) + Math.abs(other.y - y)
	fun dist(ox: Int, oy: Int) = Math.abs(ox - x) + Math.abs(oy - y)
	fun euclideanDist(other: Point) = Vector2.dst(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	fun euclideanDist(ox: Float, oy:Float) = Vector2.dst(x.toFloat(), y.toFloat(), ox, oy)
	fun euclideanDist2(other: Point) = Vector2.dst2(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	fun euclideanDist2(ox: Float, oy:Float) = Vector2.dst2(x.toFloat(), y.toFloat(), ox, oy)

	operator fun times(other: Int) = obtain().set(x * other, y * other)

	operator fun times(other: Matrix3): Point
	{
		val vec = Pools.obtain(Vector3::class.java)

		vec.set(x.toFloat(), y.toFloat(), 0f);
		vec.mul(other)
		x = vec.x.toInt()
		y = vec.y.toInt()

		Pools.free(vec)

		return this
	}

	operator fun plus(other: Direction) = obtain().set(x + other.x, y + other.y)
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

	operator fun compareTo(other: Point): Int
	{
		val compX = x.compareTo(other.x)
		if (compX != 0) return compX

		val compY = y.compareTo(other.y)
		return compY
	}

	override fun toString(): String
	{
		return "" + x + ", " + y
	}

	override fun hashCode(): Int
	{
		return toString().hashCode()
	}

    override fun reset()
    {

    }
}
