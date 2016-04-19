package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Enums
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class Level()
{
	var grid: Array2D<Tile> = Array2D()
		set(value)
		{
			field = value

			for (x in 0..width-1)
			{
				for (y in 0..height-1)
				{
					grid[x, y].level = this
					grid[x, y].x = x
					grid[x, y].y = y

					for (dir in Enums.Direction.Values)
					{
						grid[x, y].neighbours.put( dir, getTile(x, y, dir) )
					}
				}
			}
		}

	lateinit var player: Entity

	val width: Int
		get() = grid.xSize

	val height: Int
		get() = grid.ySize

	// ----------------------------------------------------------------------
	val rooms: com.badlogic.gdx.utils.Array<Room> = com.badlogic.gdx.utils.Array()

	// ----------------------------------------------------------------------
	fun getRoom(point: Point) = getRoom(point.x, point.y)
	fun getRoom(x: Int, y: Int): Room?
	{
		for (room in rooms)
		{
			if (room.x <= x && room.x+room.width >= x && room.y <= y && room.y+room.height >= y)
			{
				return room
			}
		}
		return null
	}

	// ----------------------------------------------------------------------
	fun getTile(point: Point) = getTile(point.x, point.y)

	// ----------------------------------------------------------------------
	fun getTile(point: Point, ox:Int, oy:Int) = getTile(point.x + ox, point.y + oy)

	// ----------------------------------------------------------------------
	fun getTile(point: Point, o: Point) = getTile(point.x + o.x, point.y + o.y)

	// ----------------------------------------------------------------------
	fun getTile(x: Int, y: Int, dir: Enums.Direction) = getTile(x + dir.x, y + dir.y)

	// ----------------------------------------------------------------------
	fun getTile(point: Point, dir: Enums.Direction) = getTile(point.x + dir.x, point.y + dir.y)

	// ----------------------------------------------------------------------
	fun getTile(x: Int, y: Int): Tile?
	{
		if (x < 0 || x >= width || y < 0 || y >= height)
		{
			return null
		}
		else
		{
			return grid[x, y]
		}
	}

	// ----------------------------------------------------------------------
	fun buildTilingBitflag(bitflag: EnumBitflag<Enums.Direction>, x: Int, y: Int, id: Long)
	{
		// Build bitflag of surrounding tiles
		bitflag.clear();
		for (dir in Enums.Direction.Values)
		{
			val tile = getTile( x, y, dir );

			if (tile != null)
			{
				// Attempt to find match
				var matchFound = false;

				for (entity in tile.contents)
				{
					val tilingSprite = Mappers.tilingSprite.get(entity)
					if (tilingSprite != null && tilingSprite.sprite.checkID == id)
					{
						matchFound = true
						break;
					}
				}

				if (!matchFound)
				{
					bitflag.setBit( dir );
				}
			}
		}
	}
}