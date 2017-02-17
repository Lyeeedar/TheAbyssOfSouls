package com.lyeeedar.Pathfinding

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

object BresenhamLine
{

	@JvmOverloads fun <T: IPathfindingTile>lineNoDiag(x0: Int, y0: Int, x1: Int, y1: Int, grid: Array2D<T>, checkPassable: Boolean = false, range: Int = 0, travelType: SpaceSlot? = null, self: Any? = null): Array<Point>
	{
		var x0 = x0
		var y0 = y0
		val xDist = Math.abs(x1 - x0)
		val yDist = -Math.abs(y1 - y0)
		val xStep = if (x0 < x1) +1 else -1
		val yStep = if (y0 < y1) +1 else -1
		var error = xDist + yDist

		val path = Array<Point>()

		path.add(Point.obtain().set(x0, y0))

		while (x0 != x1 || y0 != y1)
		{
			if (2 * error - yDist > xDist - 2 * error)
			{
				// horizontal step
				error += yDist
				x0 += xStep
			}
			else
			{
				// vertical step
				error += xDist
				y0 += yStep
			}

			if (x0 < 0 || y0 < 0 || x0 >= grid.xSize - 1 || y0 >= grid.ySize - 1 || checkPassable && !grid[x0, y0].getPassable(travelType!!, self))
			{
				break
			}

			path.add(Point.obtain().set(x0, y0))
		}

		return path
	}

	fun line(x: Int, y: Int, x2: Int, y2: Int, grid: Array2D<IPathfindingTile>, checkPassable: Boolean, range: Int, travelType: SpaceSlot, self: Any): Array<Point>?
	{
		var x = x
		var y = y
		var x2 = x2
		var y2 = y2
		x = MathUtils.clamp(x, 0, grid.xSize - 1)
		x2 = MathUtils.clamp(x2, 0, grid.xSize - 1)
		y = MathUtils.clamp(y, 0, grid.ySize - 1)
		y2 = MathUtils.clamp(y2, 0, grid.ySize - 1)

		if (x == x2 && y == y2)
		{
			return null
		}

		val w = x2 - x
		val h = y2 - y

		var dx1 = 0
		var dy1 = 0
		var dx2 = 0
		var dy2 = 0

		if (w < 0)
			dx1 = -1
		else if (w > 0) dx1 = 1
		if (h < 0)
			dy1 = -1
		else if (h > 0) dy1 = 1
		if (w < 0)
			dx2 = -1
		else if (w > 0) dx2 = 1

		var longest = Math.abs(w)
		var shortest = Math.abs(h)

		if (longest <= shortest)
		{
			longest = Math.abs(h)
			shortest = Math.abs(w)

			if (h < 0)
				dy2 = -1
			else if (h > 0) dy2 = 1
			dx2 = 0
		}

		var numerator = longest shr 1

		val dist = range

		val path = Array<Point>()

		for (i in 0..dist)
		{
			path.add(Point.obtain().set(x, y))

			numerator += shortest
			if (numerator >= longest)
			{
				numerator -= longest
				x += dx1
				y += dy1
			} else
			{
				x += dx2
				y += dy2
			}

			if (x < 0 || y < 0 || x >= grid.xSize - 1 || y >= grid.ySize - 1 || checkPassable && !grid[x, y].getPassable(travelType, self))
			{
				break
			}
		}

		return path
	}
}
