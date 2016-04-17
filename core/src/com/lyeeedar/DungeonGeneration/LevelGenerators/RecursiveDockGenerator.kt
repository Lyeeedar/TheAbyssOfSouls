package com.lyeeedar.DungeonGeneration.LevelGenerators

import com.PaulChew.Pnt
import com.PaulChew.Triangle
import com.PaulChew.Triangulation
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.DungeonGeneration.Data.SymbolicCorridorData
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoom
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator
import com.lyeeedar.DungeonGeneration.RoomGenerators.Basic
import com.lyeeedar.Enums
import com.lyeeedar.Level.Level
import com.lyeeedar.Pathfinding.AStarPathfind
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.ran
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class RecursiveDockGenerator(): AbstractLevelGenerator()
{
	val minPadding: Int by lazy { (levelData.corridor.width / 2) + 1 }
	val maxPadding: Int by lazy { minPadding + 3 }
	var minRoomSize: Int = 7
	var maxRoomSize: Int = 25
	val paddedMinRoom: Int by lazy { minRoomSize + minPadding * 2 }

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
			printGrid()

			if (toBePlacedRooms.size == 0)
			{
				break
			}

			size += 10
		}

		for ( room in placedRooms )
		{
			val actual = SymbolicRoom()
			room.resolveSymbols(levelData.symbolMap)
			actual.fill(ran, room, levelData)
			actual.findDoors(ran);
			levelData.levelTheme?.apply(actual, ran)

			rooms.add(actual)
		}

		markRooms()
		connectRooms()
		//placeFactions()
		markRooms()

		printGrid()
	}

	// ----------------------------------------------------------------------
	fun markRooms()
	{
		for ( room in rooms )
		{
			for (x in 0..room.width-1)
			{
				for (y in 0..room.height-1)
				{
					contents[x+room.x, y+room.y] = room.contents[x, y].copy()
				}
			}
		}
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
			if (room.placement != Enums.Direction.CENTRE)
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

			if (levelData.roomGenerators.size > 0)
			{
				val genData = levelData.roomGenerators.ran(ran)
				room.generator = AbstractRoomGenerator.load(genData)
			}
			else
			{
				room.generator = Basic()
			}
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

	// ----------------------------------------------------------------------
	protected fun connectRooms()
	{
		val roomPnts = com.badlogic.gdx.utils.Array<Pnt>()

		for (room in rooms)
		{
			for (door in room.doors)
			{
				var x = door.x + room.x
				var y = door.y + room.y

				if (door.dir == Enums.Direction.WEST)
				{
					x -= levelData.corridor.width - 1
				}
				else if (door.dir == Enums.Direction.NORTH)
				{
					y -= levelData.corridor.width - 1
				}

				if (x >= 1 && y >= 1 && x < width - 1 && y < height - 1)
				{
					val p = Pnt(x.toDouble(), y.toDouble())
					roomPnts.add(p)
				}
			}
		}

		val initialTriangle = Triangle(Pnt(-10000.0, -10000.0), Pnt(10000.0, -10000.0), Pnt(0.0, 10000.0))
		val dt = Triangulation(initialTriangle)

		for (p in roomPnts)
		{
			dt.delaunayPlace(p)
		}

		val ignoredPaths = com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>()
		val addedPaths = com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>()
		val paths = com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>()

		val tris = com.badlogic.gdx.utils.Array<Triangle>()
		for (tri in dt)
		{
			tris.add(tri)
		}
		tris.sort();

		for (tri in tris)
		{
			calculatePaths(paths, tri, ignoredPaths, addedPaths)
		}

		for (room in roomPnts)
		{
			var closest: Pnt? = null
			var closestDist = Double.MAX_VALUE
			var found = false
			outer@ for (path in paths)
			{
				var pair = arrayOf(path.first, path.second)
				for (p in pair)
				{
					if (room[0] == p[0] && room[1] == p[1])
					{
						found = true
						break@outer
					}

					val tempDist = Math.max(Math.abs(p[0] - room[0]), Math.abs(p[1] - room[1]))
					if (tempDist < closestDist)
					{
						closestDist = tempDist
						closest = p
					}
				}
			}

			if (!found)
			{
				paths.add(Pair<Pnt, Pnt>(room, closest!!))
			}
		}

		for (p in paths)
		{
			val pathFind = AStarPathfind(contents.array, p.first[0].toInt(), p.first[1].toInt(), p.second[0].toInt(), p.second[1].toInt(), false, true, levelData.corridor.width, Enums.SpaceSlot.ENTITY, null)
			val path = pathFind.path

			carveCorridor(path)
			Point.freeAll(path)
		}
	}

	// ----------------------------------------------------------------------
	protected fun carveCorridor(path: com.badlogic.gdx.utils.Array<Point>)
	{
		val width = levelData.corridor.width

		for (i in 0..path.size - 1)
		{
			val pos = path[i]

			for (x in 0..width - 1)
			{
				for (y in 0..width - 1)
				{
					val t = contents[pos.x + x, pos.y + y]

					if (t.char == '#')
					{
						contents[pos.x + x, pos.y + y] = levelData.symbolMap['.']
						t.resolve(levelData.symbolMap)
					}

					t.passable = true
					t.influence = 0
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	protected fun calculatePaths(paths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>, triangle: Triangle, ignoredPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>, addedPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>)
	{
		val vertices = triangle.toArray(Array(0){ i -> Pnt(0.0, 0.0)})

		var ignore = 0
		var dist: Double

		dist = Math.pow(2.0, vertices[0][0] - vertices[1][0]) + Math.pow(2.0, vertices[0][1] - vertices[1][1])

		var temp = Math.pow(2.0, vertices[0][0] - vertices[2][0]) + Math.pow(2.0, vertices[0][1] - vertices[2][1])
		if (dist < temp)
		{
			dist = temp
			ignore = 1
		}

		temp = Math.pow(2.0, vertices[1][0] - vertices[2][0]) + Math.pow(2.0, vertices[1][1] - vertices[2][1])
		if (dist < temp)
		{
			ignore = 2
		}

		if (ignore != 0 && !checkIgnored(vertices[0], vertices[1], ignoredPaths) && !checkAdded(vertices[0], vertices[1], addedPaths))
		{
			addPath(vertices[0], vertices[1], paths, ignoredPaths, addedPaths)
		}
		else
		{
			ignoredPaths.add(Pair(vertices[0], vertices[1]))
		}

		if (ignore != 1 && !checkIgnored(vertices[0], vertices[2], ignoredPaths) && !checkAdded(vertices[0], vertices[2], addedPaths))
		{
			addPath(vertices[0], vertices[2], paths, ignoredPaths, addedPaths)
		}
		else
		{
			ignoredPaths.add(Pair(vertices[0], vertices[2]))
		}

		if (ignore != 2 && !checkIgnored(vertices[1], vertices[2], ignoredPaths) && !checkAdded(vertices[1], vertices[2], addedPaths))
		{
			addPath(vertices[1], vertices[2], paths, ignoredPaths, addedPaths)
		}
		else
		{
			ignoredPaths.add(Pair(vertices[1], vertices[2]))
		}
	}

	// ----------------------------------------------------------------------
	protected fun addPath(p1: Pnt, p2: Pnt, paths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>, ignoredPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>, addedPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>)
	{
		if (p1[0] < 0
				|| p1[1] < 0
				|| p1[0] >= width - 1
				|| p1[1] >= height - 1
				|| p2[0] < 0
				|| p2[1] < 0
				|| p2[0] >= width - 1
				|| p2[1] >= height - 1)
		{
			ignoredPaths.add(Pair(p1, p2))
		}
		else
		{
			addedPaths.add(Pair(p1, p2))
			paths.add(Pair(p1, p2))
		}
	}

	// ----------------------------------------------------------------------
	protected fun checkIgnored(p1: Pnt, p2: Pnt, ignoredPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>): Boolean
	{
		for (p in ignoredPaths)
		{
			if (p.first.equals(p1) && p.second.equals(p2))
			{
				return true
			}
			else if (p.first.equals(p2) && p.second.equals(p1))
			{
				return true
			}
		}
		return false
	}

	// ----------------------------------------------------------------------
	protected fun checkAdded(p1: Pnt, p2: Pnt, addedPaths: com.badlogic.gdx.utils.Array<Pair<Pnt, Pnt>>): Boolean
	{
		for (p in addedPaths)
		{
			if (p.first.equals(p1) && p.second.equals(p2))
			{
				return true
			}
			else if (p.first.equals(p2) && p.second.equals(p1))
			{
				return true
			}
		}
		return false
	}
}