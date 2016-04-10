package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.Enums
import com.lyeeedar.Level.Level
import com.lyeeedar.Util.Point
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class RecursiveDockGenerator(): AbstractLevelGenerator()
{
	var minPadding: Int = 1
	var maxPadding: Int = 2

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun generate()
	{

	}

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
	}

	// ----------------------------------------------------------------------
	fun partitionRecursive(x: Int, y: Int, width: Int, height: Int)
	{
		val padX = Math.min(ran.nextInt(maxPadding - minPadding) + minPadding, (width - minRoomSize) / 2)
		val padY = Math.min(ran.nextInt(maxPadding - minPadding) + minPadding, (height - minRoomSize) / 2)

		val padX2 = padX * 2
		val padY2 = padY * 2

		// get the room to be placed
		var room: Room? = null

		// if the predefined rooms array has items, then try to pick one from it
		if (toBePlaced.size > 0)
		{
			// Array of indexes to be tried, stops duplicate work
			val indexes = Array<Int>()
			for (i in 0..toBePlaced.size - 1)
			{
				indexes.add(i)
			}

			while (room == null && indexes.size > 0)
			{
				val index = indexes.removeIndex(ran.nextInt(indexes.size))

				val testRoom = toBePlaced.get(index)

				var fits = false
				var rotate = false
				var flipVert = false
				var flipHori = false

				val fitsVertical = testRoom.width + padX2 <= width && testRoom.height + padY2 <= height
				val fitsHorizontal = testRoom.height + padX2 <= width && testRoom.width + padY2 <= height

				if (testRoom.roomData.lockRotation)
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
					toBePlaced.removeIndex(index)

					if (flipVert)
					{
						room!!.flipVertical()
					}

					if (flipHori)
					{
						room!!.flipHorizontal()
					}

					if (rotate)
					{
						room!!.rotate()
					}

					if (flipVert && rotate)
					{
						room!!.orientation = Direction.WEST
					} else if (flipVert)
					{
						room!!.orientation = Direction.SOUTH
					} else if (rotate)
					{
						room!!.orientation = Direction.EAST
					} else
					{
						room!!.orientation = Direction.NORTH
					}
				}
			}
		}

		// failed to find a suitable predefined room, so create a new one
		if (room == null && dfp.roomGenerators.size > 0)
		{
			val roomWidth = Math.min(ran.nextInt(maxRoomSize - minRoomSize) + minRoomSize, width - padX2)
			val roomHeight = Math.min(ran.nextInt(maxRoomSize - minRoomSize) + minRoomSize, height - padY2)

			room = Room()
			room!!.width = roomWidth
			room!!.height = roomHeight

			room!!.generateRoomContents(ran, dfp)
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
			room!!.x = x + padX
			room!!.y = y + padY
		} else if (side == 1)
		{
			room!!.x = x + width - (room!!.width + padX)
			room!!.y = y + padY
		} else if (side == 2)
		{
			room!!.x = x + padX
			room!!.y = y + height - (room!!.height + padY)
		} else
		{
			room!!.x = x + width - (room!!.width + padX)
			room!!.y = y + height - (room!!.height + padY)
		}

		// split into 2 remaining rectangles and recurse
		if (side == 0)
		{
			// r1
			// 22
			run {
				val nx = room!!.x + room!!.width + padX
				val ny = y
				val nwidth = x + width - nx
				val nheight = room!!.height + padY2

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}

			run {
				val nx = x
				val ny = room!!.y + room!!.height + padY
				val nwidth = width
				val nheight = y + height - ny

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}
		} else if (side == 1)
		{
			// 1r
			// 12
			run {
				val nx = x
				val ny = y
				val nwidth = width - (room!!.width + padX2)
				val nheight = height

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}

			run {
				val nx = room!!.x - padX
				val ny = room!!.y + room!!.height + padY
				val nwidth = room!!.width + padX2
				val nheight = y + height - ny

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}
		} else if (side == 2)
		{
			// 12
			// r2
			run {
				val nx = x
				val ny = y
				val nwidth = room!!.width + padX2
				val nheight = height - (room!!.height + padY2)

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}

			run {
				val nx = x + room!!.width + padX2
				val ny = y
				val nwidth = x + width - nx
				val nheight = height

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}
		} else
		{
			// 22
			// 1r
			run {
				val nx = x
				val ny = room!!.y - padY
				val nwidth = width - (room!!.width + padX2)
				val nheight = y + height - ny

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}

			run {
				val nx = x
				val ny = y
				val nwidth = width
				val nheight = height - (room!!.height + padY2)

				if (nwidth >= paddedMinRoom && nheight >= paddedMinRoom)
				{
					partitionRecursive(nx, ny, nwidth, nheight)
				}
			}
		}
	}
}