package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.DungeonGeneration.Data.SymbolicCorridorData
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoom
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator
import com.lyeeedar.Enums
import com.lyeeedar.Level.Level
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.ran
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class RecursiveDockGenerator(): AbstractLevelGenerator()
{
	var minPadding: Int = 1
	var maxPadding: Int = 2
	var minRoomSize: Int = 3
	var maxRoomSize: Int = 10
	val paddedMinRoom: Int
		get() = minRoomSize + minPadding * 2

	var size: Int = 10

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun generate()
	{
		selectRooms();

		while (true)
		{
			toBePlacedRooms.clear();
			placedRooms.clear();
			rooms.clear()

			toBePlacedRooms.addAll( chosenRooms );

			fillGridBase();
			partition( );

			if (toBePlacedRooms.size == 0)
			{
				break
			}
		}

		for ( room in placedRooms )
		{
			val actual = SymbolicRoom()
			actual.fill(ran, room)
			actual.findDoors( ran );

			rooms.add(actual)
		}

		markRooms()
		connectRooms()
		placeFactions()
		markRooms()
	}

	// ----------------------------------------------------------------------
	fun fillGridBase()
	{
		val wall = levelData.symbolMap['#']
		wall.resolve(levelData.symbolMap)

		contents = Array2D(size, size) { x, y -> wall.copy() }

		if (levelData.preprocessor != null)
		{
			val generator = AbstractRoomGenerator.load(levelData.preprocessor!!)
			generator.process(contents, levelData.symbolMap, ran)

			if (generator.ensuresConnectivity)
			{
				for (x in 0..size - 1)
				{
					for (y in 0..size - 1)
					{
						if (contents[x, y].char == '#')
						{
							contents[x, y].passable = false
						}
					}
				}
			}
		}

		for (x in 0..size-1)
		{
			for (y in 0..size - 1)
			{
				if (x == 0 || y == 0 || x == size-1 || y == size-1)
				{
					contents[x, y] = wall.copy()
					contents[x, y].passable = false
				}
				else
				{
					contents[x, y].passable = true
				}

				contents[x, y].influence = when(levelData.corridor.style)
				{
					SymbolicCorridorData.Style.WANDERING -> ran.nextInt( ( width + height ) / 2 ) + ( width + height ) / 2
					else -> width + height
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun partition()
	{
		val min = Point(0, 0)
		val max = Point(width, height)

		// place edge rooms
		val edges = EnumMap<Enums.Direction, com.badlogic.gdx.utils.Array<SymbolicRoomData>>(Enums.Direction::class.java)

		val itr = toBePlacedRooms.iterator()
		while (itr.hasNext())
		{
			val room = itr.next()
			if (room.placement != Enums.Direction.CENTER)
			{
				itr.remove()

				var block = edges[room.placement]
				if (block == null)
				{
					block = com.badlogic.gdx.utils.Array<SymbolicRoomData>()
					edges.put(room.placement, block)
				}

				block.add(room)
			}
		}

		for (dir in Enums.Direction.CardinalValues)
		{
			val block = edges[dir] ?: continue

			val vertical = dir.x != 0
			var totalSize = 0

			for (room in block)
			{
				room.ran = ran
				totalSize += if (vertical) room.heightVal else room.widthVal

				if (max.x - min.x >= room.widthVal + minPadding || max.y - min.y >= room.heightVal + minPadding)
				{
					// not enough space
					return
				}
			}

			val space = if (vertical) max.y - min.y else max.x - min.x
			if (totalSize > space) return // not enough space

			val spacing = Math.floor((space - totalSize).toDouble() / (block.size + 1).toDouble()).toInt()

			var maxPoint = 0
			var current = ran.nextInt(spacing * 2)
			var extra = spacing * 2 - current

			for (room in block)
			{
				room.x = if (!vertical) current else if (dir.x < 0) min.x else max.x - room.widthVal
				room.y = if (vertical) current else if (dir.y < 0) min.y else max.y - room.heightVal

				val sizeVal = if (vertical) room.heightVal else room.widthVal
				val spaceVal = if (!vertical) room.heightVal else room.widthVal
				if (sizeVal > maxPoint) maxPoint = sizeVal

				placedRooms.add(room)

				val offset = ran.nextInt(spacing + extra)
				extra = (spacing + extra) - offset
				current += spaceVal + offset
			}

			if (!vertical) if (dir.y < 0) min.y += maxPoint else max.y -= maxPoint
			if (vertical) if (dir.x < 0) min.x += maxPoint else max.x -= maxPoint
		}

		// launch recursive phase
		val w = max.x-min.x
		val h = max.y-min.y
		if ( w >= paddedMinRoom && h >= paddedMinRoom )
		{
			partitionRecursive( min.x + minPadding, min.y + minPadding, w - minPadding*2, h - minPadding*2 );
		}
	}

	// ----------------------------------------------------------------------
	fun partitionRecursive(x: Int, y: Int, width: Int, height: Int)
	{
		val padX = Math.min(ran.nextInt(maxPadding - minPadding) + minPadding, (width - minRoomSize) / 2)
		val padY = Math.min(ran.nextInt(maxPadding - minPadding) + minPadding, (height - minRoomSize) / 2)

		val padX2 = padX * 2
		val padY2 = padY * 2

		// get the room to be placed
		var room: SymbolicRoomData? = null

		// if the predefined rooms array has items, then try to pick one from it
		if (toBePlacedRooms.size > 0)
		{
			// Array of indexes to be tried, stops duplicate work
			val indexes = com.badlogic.gdx.utils.Array<Int>()
			for (i in 0..toBePlacedRooms.size - 1)
			{
				indexes.add(i)
			}

			while (room == null && indexes.size > 0)
			{
				val index = indexes.removeIndex(ran.nextInt(indexes.size))

				val testRoom = toBePlacedRooms.get(index)

				var fits = false
				var rotate = false
				var flipVert = false
				var flipHori = false

				val fitsVertical = testRoom.widthVal + padX2 <= width && testRoom.heightVal + padY2 <= height
				val fitsHorizontal = testRoom.heightVal + padX2 <= width && testRoom.widthVal + padY2 <= height

				if (testRoom.lockRotation)
				{
					if (fitsVertical)
					{
						fits = true
						flipVert = true

						if (ran.nextBoolean())
						{
							flipHori = true
						}
					}
				} else
				{
					if (fitsVertical || fitsHorizontal)
					{
						fits = true

						// randomly flip
						if (ran.nextBoolean())
						{
							flipVert = true
						}

						if (ran.nextBoolean())
						{
							flipHori = true
						}

						// if it fits on both directions, randomly pick one
						if (fitsVertical && fitsHorizontal)
						{
							if (ran.nextBoolean())
							{
								rotate = true
							}
						} else if (fitsHorizontal)
						{
							rotate = true
						}
					}
				}

				// If it fits then place the room and rotate/flip as neccesary
				if (fits)
				{
					room = testRoom
					toBePlacedRooms.removeIndex(index)

					room.flipVert = flipVert
					room.flipHori = flipHori
					room.rotate = rotate
				}
			}
		}

		// failed to find a suitable predefined room, so create a new one
		if (room == null && levelData.roomGenerators.size > 0)
		{
			val roomWidth = Math.min(ran.nextInt(maxRoomSize - minRoomSize) + minRoomSize, width - padX2)
			val roomHeight = Math.min(ran.nextInt(maxRoomSize - minRoomSize) + minRoomSize, height - padY2)

			room = SymbolicRoomData()
			room.width = roomWidth.toString()
			room.height = roomHeight.toString()
			room.ran = ran

			val genData = levelData.roomGenerators.ran(ran)
			room.generator = AbstractRoomGenerator.load(genData)
		}

		if (room == null)
		{
			return
		}

		placedRooms.add(room)

		// pick corner

		// possible sides:
		// 0 1
		// 2 3
		val side = ran.nextInt(4)

		// Position room at side
		if (side == 0)
		{
			room.x = x + padX
			room.y = y + padY
		}
		else if (side == 1)
		{
			room.x = x + width - (room.widthVal + padX)
			room.y = y + padY
		}
		else if (side == 2)
		{
			room.x = x + padX
			room.y = y + height - (room.heightVal + padY)
		}
		else
		{
			room.x = x + width - (room.widthVal + padX)
			room.y = y + height - (room.heightVal + padY)
		}

		// split into 2 remaining rectangles and recurse
		if (side == 0)
		{
			// r1
			// 22
			val nx1 = room.x + room.widthVal + padX
			val ny1 = y
			val nwidth1 = x + width - nx1
			val nheight1 = room.heightVal + padY2

			if (nwidth1 >= paddedMinRoom && nheight1 >= paddedMinRoom)
			{
				partitionRecursive(nx1, ny1, nwidth1, nheight1)
			}

			val nx2 = x
			val ny2 = room.y + room.heightVal + padY
			val nwidth2 = width
			val nheight2 = y + height - ny2

			if (nwidth2 >= paddedMinRoom && nheight2 >= paddedMinRoom)
			{
				partitionRecursive(nx2, ny2, nwidth2, nheight2)
			}
		}
		else if (side == 1)
		{
			// 1r
			// 12
			val nx1 = x
			val ny1 = y
			val nwidth1 = width - (room.widthVal + padX2)
			val nheight1 = height

			if (nwidth1 >= paddedMinRoom && nheight1 >= paddedMinRoom)
			{
				partitionRecursive(nx1, ny1, nwidth1, nheight1)
			}

			val nx2 = room.x - padX
			val ny2 = room.y + room.heightVal + padY
			val nwidth2 = room.widthVal + padX2
			val nheight2 = y + height - ny2

			if (nwidth2 >= paddedMinRoom && nheight2 >= paddedMinRoom)
			{
				partitionRecursive(nx2, ny2, nwidth2, nheight2)
			}
		}
		else if (side == 2)
		{
			// 12
			// r2
			val nx1 = x
			val ny1 = y
			val nwidth1 = room.widthVal + padX2
			val nheight1 = height - (room.heightVal + padY2)

			if (nwidth1 >= paddedMinRoom && nheight1 >= paddedMinRoom)
			{
				partitionRecursive(nx1, ny1, nwidth1, nheight1)
			}

			val nx2 = x + room.widthVal + padX2
			val ny2 = y
			val nwidth2 = x + width - nx2
			val nheight2 = height

			if (nwidth2 >= paddedMinRoom && nheight2 >= paddedMinRoom)
			{
				partitionRecursive(nx2, ny2, nwidth2, nheight2)
			}
		}
		else
		{
			// 22
			// 1r
			val nx1 = x
			val ny1 = room.y - padY
			val nwidth1 = width - (room.widthVal + padX2)
			val nheight1 = y + height - ny1

			if (nwidth1 >= paddedMinRoom && nheight1 >= paddedMinRoom)
			{
				partitionRecursive(nx1, ny1, nwidth1, nheight1)
			}

			val nx2 = x
			val ny2 = y
			val nwidth2 = width
			val nheight2 = height - (room.heightVal + padY2)

			if (nwidth2 >= paddedMinRoom && nheight2 >= paddedMinRoom)
			{
				partitionRecursive(nx2, ny2, nwidth2, nheight2)
			}
		}
	}
}