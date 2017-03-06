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

const val X_HASH_SIZE = 10000

open class Point : Pool.Poolable, Comparable<Point>
{
	var dirty = true
	var locked: Boolean = false
	var fromPool: Boolean = false

	var x: Int = 0
		set(value)
		{
			if (locked) throw RuntimeException("Tried to edit a locked point")
			if (fromPool && !obtained) throw RuntimeException("Tried to edit a freed point")
			if (field != value) dirty = true
			field = value
		}

	var y: Int = 0
		set(value)
		{
			if (locked) throw RuntimeException("Tried to edit a locked point")
			if (fromPool && !obtained) throw RuntimeException("Tried to edit a freed point")
			if (field != value) dirty = true
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

	lateinit var obtainPath: String
//	protected fun finalize()
//	{
//		if (obtained)
//		{
//			if (leakMap.containsKey(obtainPath))
//			{
//				var oldVal = leakMap[obtainPath]
//				oldVal++
//				leakMap[obtainPath] = oldVal
//			}
//			else
//			{
//				leakMap[obtainPath] = 1
//			}
//		}
//	}

    companion object
    {
		//private val leakMap = ObjectMap<String, Long>()

		@JvmField val ZERO = Point(0, 0, true)
		@JvmField val ONE = Point(1, 1, true)
		@JvmField val MINUS_ONE = Point(-1, -1, true)
		@JvmField val MAX = Point(Int.MAX_VALUE, Int.MAX_VALUE, true)
		@JvmField val MIN = Point(-Int.MAX_VALUE, -Int.MAX_VALUE, true)

        private val pool: Pool<Point> = object : Pool<Point>() {
			override fun newObject(): Point
			{
				return Point()
			}
		}

        @JvmStatic fun obtain(): Point
		{
			val point = pool.obtain()
			point.fromPool = true
			point.locked = false

			if (point.obtained) throw RuntimeException()

			//point.obtainPath = Thread.currentThread().stackTrace.joinToString(separator = "\n") { it.toString() }
			point.obtained = true
			return point
		}

		fun obtainTS(): Point
		{
			synchronized(pool)
			{
				return obtain()
			}
		}

		@JvmStatic fun freeAll(items: Iterable<Point>) = { for (item in items) item.free() }

		fun freeAllTS(items: Iterable<Point>)
		{
			synchronized(pool)
			{
				freeAll(items)
			}
		}
    }

    private var obtained = false

	fun set(string: String): Point
	{
		val split = string.split(",")
		x = split[0].toInt()
		y = split[1].toInt()

		return this
	}

	fun set(x: Int, y: Int): Point
    {
        this.x = x
        this.y = y
        return this
    }

	fun set(hashcode: Int): Point
	{
		x = hashcode / X_HASH_SIZE
		y = hashcode - (x * X_HASH_SIZE)
		return this
	}

	inline fun set(other: Point) = set(other.x, other.y)

	inline fun copy() = Point.obtain().set(this)

	fun free() { if (obtained) { Point.pool.free(this); obtained = false; obtainPath = "" } }

	fun freeTS()
	{
		synchronized(pool)
		{
			free()
		}
	}

	inline fun taxiDist(other: Point) = Math.max( Math.abs(other.x - x), Math.abs(other.y - y) )
	inline fun dist(other: Point) = Math.abs(other.x - x) + Math.abs(other.y - y)
	inline fun dist(ox: Int, oy: Int) = Math.abs(ox - x) + Math.abs(oy - y)
	inline fun euclideanDist(other: Point) = Vector2.dst(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	inline fun euclideanDist(ox: Float, oy:Float) = Vector2.dst(x.toFloat(), y.toFloat(), ox, oy)
	inline fun euclideanDist2(other: Point) = Vector2.dst2(x.toFloat(), y.toFloat(), other.x.toFloat(), other.y.toFloat())
	inline fun euclideanDist2(ox: Float, oy:Float) = Vector2.dst2(x.toFloat(), y.toFloat(), ox, oy)

	inline fun liesInRect(min: Point, max: Point): Boolean = x >= min.x && x <= max.x && y >= min.y&& y <= max.y

	fun liesOnLine(p1: Point, p2: Point): Boolean
	{
		// early out
		if (p1 == p2) return this == p1

		if (p1.x == p2.x)
		{
			// if on same line then we can continue
			if (x == p1.x)
			{
				var miny = p1.y
				var maxy = p2.y

				// swap if in wrong order
				if (maxy < miny)
				{
					val temp = maxy
					maxy = miny
					miny = temp
				}

				return y >= miny && y <= maxy
			}
		}
		else if (p1.y == p2.y)
		{
			// if on same line then we can continue
			if (y == p1.y)
			{
				var minx = p1.x
				var maxx = p2.x

				// swap if in wrong order
				if (maxx < minx)
				{
					val temp = maxx
					maxx = minx
					minx = temp
				}

				return x >= minx && x <= maxx
			}
		}
		else
		{
			// cant determine if at an angle
			return false
		}

		// failed to find
		return false
	}

	inline fun lerp(p2: Point, alpha: Float) = obtain().set(x + ((p2.x - x) * alpha).toInt(), y + ((p2.y - y) * alpha).toInt())

	inline fun getPosDiff(p: Point): kotlin.Array<Vector2> = getPosDiff(p.x, p.y)
	inline fun getPosDiff(px: Int, py: Int): kotlin.Array<Vector2>
	{
		val oldPos = Vector2(px.toFloat(), py.toFloat())
		val newPos = Vector2(x.toFloat(), y.toFloat())

		val diff = newPos.sub(oldPos)
		diff.x *= -1

		return arrayOf(diff, Vector2())
	}

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
	operator fun div(other: Int) = obtain().set(x / other, y / other)

	operator fun timesAssign(other: Int) { x *= other; y *= other; }

	operator fun plusAssign(other: Point) { x += other.x; y += other.y }
	operator fun plusAssign(other: Direction) { x += other.x; y += other.y }
	operator fun minusAssign(other: Point) { x -= other.x; y -= other.y }
	operator fun timesAssign(other: Point) { x *= other.x; y *= other.y }
	operator fun divAssign(other: Point) { x /= other.x; y /= other.y }

	operator fun get(x: Int) = when (x)
	{
		0 -> x
		1 -> y
		else -> throw IndexOutOfBoundsException()
	}

	override fun equals(other: Any?): Boolean
	{
		if (other == null || other !is Point) return false

		return hashCode() == other.hashCode()
	}

	override operator fun compareTo(other: Point): Int
	{
		return hashCode().compareTo(other.hashCode())
	}

	operator fun rangeTo(other: Point): PointRange = PointRange(this, other)

	override fun toString(): String
	{
		return "" + x + ", " + y + " hashcode: " + hashCode()
	}

	var storedHashCode: Int = 0
	override fun hashCode(): Int
	{
		if (dirty)
		{
			if (y >= X_HASH_SIZE) throw RuntimeException("Y too large for fast hashcode path! Y: $y >= $X_HASH_SIZE")
			storedHashCode = x * X_HASH_SIZE + y
			dirty = false
		}

		return storedHashCode
	}

    override fun reset()
    {

    }
}

class PointRange(override val endInclusive: Point, override val start: Point) : ClosedRange<Point>, PointProgression(start, endInclusive)
{
	override fun contains(value: Point): Boolean
	{
		return value.liesInRect(start, endInclusive)
	}

	override fun isEmpty(): Boolean = start == endInclusive
}

open class PointProgression
	internal constructor(start: Point, end: Point): Iterable<Point>
{
	val first: Point = start
	val last: Point = end

	override fun iterator(): Iterator<Point> = PointIterator(first, last)
}

class PointIterator(val start: Point, val end: Point): Iterator<Point>
{
	var xstep: Float = 0f
	var ystep: Float = 0f
	var steps: Int = 0
	var i: Int = 0

	init
	{
		val xdiff = end.x - start.x
		val ydiff = end.y - start.y

		steps = Math.max(Math.abs(xdiff), Math.abs(ydiff))

		xstep = xdiff.toFloat() / steps.toFloat()
		ystep = ydiff.toFloat() / steps.toFloat()
	}

	override fun hasNext(): Boolean = i <= steps

	override fun next(): Point
	{
		val x = start.x + Math.round(xstep * i.toFloat()).toInt()
		val y = start.y + Math.round(ystep * i.toFloat()).toInt()
		i++

		return Point.obtain().set(x, y)
	}
}

data class MinMax(val min: Int, val max: Int)