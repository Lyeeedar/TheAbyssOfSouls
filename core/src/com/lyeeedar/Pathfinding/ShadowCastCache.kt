package com.lyeeedar.Pathfinding

import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import squidpony.squidgrid.FOV
import squidpony.squidgrid.Radius

class ShadowCastCache @JvmOverloads constructor(private val LightPassability: SpaceSlot = SpaceSlot.WALL, fovType: Int = FOV.SHADOW)
{

	constructor(fovType: Int) : this(SpaceSlot.WALL, fovType)
	{
	}



	fun copy(): ShadowCastCache
	{
		val cache = ShadowCastCache(LightPassability)
		cache.lastrange = lastrange
		cache.lastx = lastx
		cache.lasty = lasty

		for (p in opaqueTiles)
		{
			cache.opaqueTiles.add(p.copy())
		}

		for (p in currentShadowCast)
		{
			cache.currentShadowCast.add(p.copy())
		}

		return cache
	}

	private val fov: FOV

	var lastrange: Int = 0
		private set
	var lastx: Int = 0
		private set
	var lasty: Int = 0
		private set
	private val opaqueTiles = com.badlogic.gdx.utils.Array<Point>()
	private val clearTiles = com.badlogic.gdx.utils.Array<Point>()
	val currentShadowCast = com.badlogic.gdx.utils.Array<Point>()
	var rawOutput: Array<DoubleArray>? = null
		private set

	init
	{
		fov = FOV(fovType)
	}

	@JvmOverloads fun getShadowCast(grid: Array2D<Tile>, x: Int, y: Int, range: Int, caster: Any?, allowOutOfBounds: Boolean = false): com.badlogic.gdx.utils.Array<Point>
	{
		var recalculate = false

		if (x != lastx || y != lasty)
		{
			recalculate = true
		} else if (range != lastrange)
		{
			recalculate = true
		} else
		{
			for (pos in opaqueTiles)
			{
				val tile = grid[pos.x, pos.y]
				if (tile.getPassable(LightPassability, caster))
				{
					recalculate = true // something has moved
					break
				}
			}

			if (!recalculate)
			{
				for (pos in clearTiles)
				{
					val tile = grid[pos.x, pos.y]
					if (!tile.getPassable(LightPassability, caster))
					{
						recalculate = true // something has moved
						break
					}
				}
			}
		}

		if (recalculate)
		{
			Point.freeAll(currentShadowCast)
			currentShadowCast.clear()

			// build grid
			val resistanceGrid = Array(range * 2) { DoubleArray(range * 2) }
			for (ix in 0..range * 2 - 1)
			{
				for (iy in 0..range * 2 - 1)
				{
					val gx = ix + x - range
					val gy = iy + y - range

					if (grid.inBounds(gx, gy))
					{
						resistanceGrid[ix][iy] = (if (grid[gx, gy].getPassable(LightPassability, caster)) 0 else 1).toDouble()
					}
					else
					{
						resistanceGrid[ix][iy] = 1.0
					}
				}
			}

			rawOutput = fov.calculateFOV(resistanceGrid, range, range, range.toDouble(), Radius.SQUARE)

			for (ix in 0..range * 2 - 1)
			{
				for (iy in 0..range * 2 - 1)
				{
					val gx = ix + x - range
					val gy = iy + y - range

					if (rawOutput!![ix][iy] > 0 && grid.inBounds(gx, gy))
					{
						currentShadowCast.add(Point.obtain().set(gx, gy))
					}
				}
			}

			// build list of clear/opaque
			opaqueTiles.clear()
			clearTiles.clear()

			for (pos in currentShadowCast)
			{
				if (pos.x < 0 || pos.y < 0 || pos.x >= grid.xSize || pos.y >= grid.ySize)
				{
					continue
				}

				val tile = grid[pos.x, pos.y]
				if (!tile.getPassable(LightPassability, caster))
				{
					opaqueTiles.add(pos)
				} else
				{
					clearTiles.add(pos)
				}
			}
			lastx = x
			lasty = y
			lastrange = range
		}

		return currentShadowCast
	}
}
