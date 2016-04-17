package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Enums
import com.lyeeedar.Enums.Direction
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.removeRan
import squidpony.squidgrid.FOV
import java.util.*

/**
 * Created by Philip on 17-Apr-16.
 */

class RoomTheme()
{
	val featureMap: ObjectMap<SymbolicFeature.Placement, com.badlogic.gdx.utils.Array<SymbolicFeature>> = ObjectMap()

	fun isPosEnclosed(room: SymbolicRoom, x: Int, y: Int): Boolean
	{
		val solid = EnumBitflag<Direction>()
		for (dir in Direction.Values)
		{
			val x1 = x + dir.x
			val y1 = y + dir.y

			val collide = x1 >= 0 && y1 >= 0 && x1 < room.width && y1 < room.height && !room.contents[x1, y1].getPassable(Enums.SpaceSlot.WALL, null)

			if (collide)
			{
				solid.setBit(dir)
			}
		}

		// Identify open paths through this pos

		// Vertical path
		if (!solid.contains(Direction.NORTH) && !solid.contains(Direction.SOUTH))
		{
			val side1 = solid.contains(Direction.EAST) || solid.contains(Direction.NORTHEAST) || solid.contains(Direction.SOUTHEAST)
			val side2 = solid.contains(Direction.WEST) || solid.contains(Direction.NORTHWEST) || solid.contains(Direction.SOUTHWEST)

			if (side1 && side2)
			{
				return true
			}
		}

		// Horizontal path
		if (!solid.contains(Direction.EAST) && !solid.contains(Direction.WEST))
		{
			val side1 = solid.contains(Direction.NORTH) || solid.contains(Direction.NORTHEAST) || solid.contains(Direction.NORTHWEST)
			val side2 = solid.contains(Direction.SOUTH) || solid.contains(Direction.SOUTHEAST) || solid.contains(Direction.SOUTHWEST)

			if (side1 && side2)
			{
				return true
			}
		}

		return false
	}

	fun getValidHidden(validList: com.badlogic.gdx.utils.Array<Point>, feature: SymbolicFeature, room: SymbolicRoom): com.badlogic.gdx.utils.Array<Point>
	{
		val resistanceMap = Array(room.width) { i -> DoubleArray(room.height) { i -> 0.0 } }
				Array2D<Double>(room.width, room.height) { x, y -> 0.0 }
		for (x in 0..room.width-1)
		{
			for (y in 0..room.height-1)
			{
				resistanceMap[x][y] = if (room.contents[x, y].contents[Enums.SpaceSlot.WALL] != null) 1.0 else 0.0
			}
		}

		val output = com.badlogic.gdx.utils.Array<Point>()

		val fov = FOV()

		for (valid in validList)
		{
			if (feature.overwrite || room.contents[valid].contents[feature.slot] == null)
			{
				// a valid start, shadow cast to see if we can spot a door

				val sightMap = fov.calculateFOV(resistanceMap, valid.x, valid.y)
				for (door in room.doors)
				{
					if (sightMap[door.x][door.y] > 0)
					{
						output.add(valid)
						break
					}
				}
			}
		}

		return output
	}

	fun getValidCorner(validList: com.badlogic.gdx.utils.Array<Point>, feature: SymbolicFeature, room: SymbolicRoom): com.badlogic.gdx.utils.Array<Point>
	{
		val output = com.badlogic.gdx.utils.Array<Point>()

		for (valid in validList)
		{
			if (feature.overwrite || room.contents[valid].contents[feature.slot] == null)
			{
				for (dir in Direction.CardinalValues)
				{
					if (room.contents[valid, dir].contents[Enums.SpaceSlot.WALL] != null)
					{
						val cw = room.contents[valid, dir.clockwise].contents[Enums.SpaceSlot.WALL] != null
						val ccw = room.contents[valid, dir.anticlockwise].contents[Enums.SpaceSlot.WALL] != null

						if (cw || ccw)
						{
							output.add(valid)
							break
						}
					}
				}
			}
		}

		return output
	}

	fun getValidWall(validList: com.badlogic.gdx.utils.Array<Point>, feature: SymbolicFeature, room: SymbolicRoom): com.badlogic.gdx.utils.Array<Point>
	{
		val output = com.badlogic.gdx.utils.Array<Point>()

		for (valid in validList)
		{
			if (feature.overwrite || room.contents[valid].contents[feature.slot] == null)
			{
				for (dir in Direction.CardinalValues)
				{
					if (room.contents[valid, dir].contents[Enums.SpaceSlot.WALL] != null)
					{
						output.add(valid)
						break
					}
				}
			}
		}

		return output
	}

	fun getValidCentre(validList: com.badlogic.gdx.utils.Array<Point>, feature: SymbolicFeature, room: SymbolicRoom): com.badlogic.gdx.utils.Array<Point>
	{
		val output = com.badlogic.gdx.utils.Array<Point>()

		for (valid in validList)
		{
			if (feature.overwrite || room.contents[valid].contents[feature.slot] == null)
			{
				var clear = true
				for (dir in Direction.CardinalValues)
				{
					if (room.contents[valid, dir].contents[Enums.SpaceSlot.WALL] != null)
					{
						clear = false
						break
					}
				}

				if (clear)
				{
					output.add(valid)
				}
			}
		}

		return output
	}

	fun apply(room: SymbolicRoom, ran: Random)
	{
		// build lists of valid tiles
		val validList = com.badlogic.gdx.utils.Array<Point>();
		for ( x in 0..room.width-1 )
		{
			for ( y in 0..room.height-1 )
			{
				if ( room.contents[x, y].getPassable( Enums.SpaceSlot.WALL, null ) )
				{
					val point = Point.obtain().set( x, y );
					if ( x > 0 && x < room.width - 1 && y > 0 && y < room.height - 1 )
					{
						if (!isPosEnclosed( room, x, y ))
						{
							validList.add( point );
						}
					}
				}
			}
		}

		// place features
		for (placement in SymbolicFeature.Placement.Values)
		{
			val block = featureMap[placement] ?: continue
			for (feature in block)
			{
				val valid = when(placement)
				{
					SymbolicFeature.Placement.HIDDEN -> getValidHidden(validList, feature, room)
					SymbolicFeature.Placement.CORNER -> getValidCorner(validList, feature, room)
					SymbolicFeature.Placement.WALL -> getValidWall(validList, feature, room)
					SymbolicFeature.Placement.CENTRE -> getValidCentre(validList, feature, room)
					else -> validList
				}

				if (valid.size > 0)
				{
					var count = if (feature.useCount) feature.count else (valid.size.toFloat() * feature.coverage).toInt()
					while (count > 0)
					{
						val pos = valid.removeRan(ran)
						room.contents[pos].contents[feature.slot] = feature.entity
						room.contents[pos].char = 'E'

						count--
					}
				}
				else
				{
					System.err.println("No valid point for placement type: $placement")
				}
			}
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element): RoomTheme
		{
			val theme = RoomTheme()

			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)
				val feature = SymbolicFeature.load(el)

				if (theme.featureMap.get(feature.placement) == null)
				{
					theme.featureMap.put(feature.placement, com.badlogic.gdx.utils.Array<SymbolicFeature>())
				}

				theme.featureMap.get(feature.placement).add(feature)
			}

			return theme
		}
	}
}