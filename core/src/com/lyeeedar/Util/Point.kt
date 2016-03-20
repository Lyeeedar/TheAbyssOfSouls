package com.lyeeedar.Util

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

/**
 * Created by Philip on 20-Mar-16.
 */

open class Point constructor(@JvmField var x: Int = 0, @JvmField var y: Int = 0) : Pool.Poolable
{
    constructor( other: Point ) : this(other.x, other.y)

    companion object
    {
        @JvmField val pool: Pool<Point> = Pools.get( Point::class.java, Int.MAX_VALUE )

        @JvmStatic fun obtain() = Point.pool.obtain()
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

    override fun reset()
    {
        if (!obtained) throw RuntimeException()
        obtained = false

        x = 0
        y = 0
    }
}
