package com.lyeeedar.Util

import com.badlogic.gdx.math.Path
import com.badlogic.gdx.math.Vector2

/**
 * Created by Philip on 19-Jul-16.
 */

class UnsmoothedPath(val path: Array<Vector2>): Path<Vector2>
{
	val distPoints: FloatArray = FloatArray(path.size)

	init
	{
		for (i in 1..path.size-1)
		{
			distPoints[i] = distPoints[i-1] + path[i-1].dst(path[i])
		}
	}

	/** @return The value of the path at t where 0<=t<=1 */
	override fun valueAt (out: Vector2, t: Float): Vector2
	{
		if (t >= 1f)
		{
			out.set(path.last())
			return out
		}

		if (t <= 0f)
		{
			out.set(path.first())
			return out
		}

		val targetDst = distPoints.last() * t
		var i = 0
		while (distPoints[i] < targetDst) i++

		if (i == 0)
		{
			out.set(path[0])
			return out
		}
		else
		{
			val p1 = path[i-1]
			val p2 = path[i]

			val diff = distPoints[i] - distPoints[i-1]
			val alphaDiff = targetDst - distPoints[i-1]
			val a = alphaDiff / diff

			out.set(p1).lerp(p2, a)

			return out
		}
	}

	override fun locate(v: Vector2?): Float
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun approxLength(samples: Int): Float
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun approximate(v: Vector2?): Float
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun derivativeAt(out: Vector2?, t: Float): Vector2
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}