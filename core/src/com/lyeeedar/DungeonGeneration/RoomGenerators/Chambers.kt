package com.lyeeedar.DungeonGeneration.RoomGenerators

import java.util.Random

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Direction
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.ran

/**
 * Seperates the room via Binary Space partitioning, then places doors to
 * connect the branches of the tree.

 * @author Philip Collin
 */
class Chambers : AbstractRoomGenerator(true)
{

	class BSPTree(var x: Int, var y: Int, var width: Int, var height: Int)
	{

		var child1: BSPTree? = null
		var child2: BSPTree? = null
		var splitVertically: Boolean = false

		fun partition(ran: Random)
		{
			if (width < minSize * 2 && height < minSize * 2 || width < maxSize && height < maxSize && ran.nextInt(5) == 0)
			{

			}
			else if (width < minSize * 2)
			{
				val split = 0.3f + ran.nextFloat() * 0.4f
				val splitheight = (height * split).toInt()

				child1 = BSPTree(x, y, width, splitheight)
				child2 = BSPTree(x, y + splitheight, width, height - splitheight)

				splitVertically = true

				child1!!.partition(ran)
				child2!!.partition(ran)

			}
			else if (height < minSize * 2)
			{
				val split = 0.3f + ran.nextFloat() * 0.4f
				val splitwidth = (width * split).toInt()

				child1 = BSPTree(x, y, splitwidth, height)
				child2 = BSPTree(x + splitwidth, y, width - splitwidth, height)

				splitVertically = false

				child1!!.partition(ran)
				child2!!.partition(ran)
			}
			else
			{
				val vertical = ran.nextBoolean()
				if (vertical)
				{
					val split = 0.3f + ran.nextFloat() * 0.4f
					val splitwidth = (width * split).toInt()

					child1 = BSPTree(x, y, splitwidth, height)
					child2 = BSPTree(x + splitwidth, y, width - splitwidth, height)

					splitVertically = false

					child1!!.partition(ran)
					child2!!.partition(ran)
				}
				else
				{
					val split = 0.3f + ran.nextFloat() * 0.4f
					val splitheight = (height * split).toInt()

					child1 = BSPTree(x, y, width, splitheight)
					child2 = BSPTree(x, y + splitheight, width, height - splitheight)

					splitVertically = true

					child1!!.partition(ran)
					child2!!.partition(ran)
				}
			}
		}

		private fun placeDoor(grid: Array<IntArray>, ran: Random)
		{
			val gridWidth = grid.size
			val gridHeight = grid[0].size
			val possibleDoorTiles = com.badlogic.gdx.utils.Array<Point>()

			if (splitVertically)
			{
				for (ix in 0..width - 1)
				{
					val tx = x + ix
					val ty = child2!!.y

					var valid = true
					if (valid)
					{
						val ttx = tx
						val tty = ty - 1
						if (tty >= 0 && grid[ttx][tty] != 1)
						{
							valid = false
						}
					}

					if (valid)
					{
						val ttx = tx
						val tty = ty + 1
						if (tty < gridHeight && grid[ttx][tty] != 1)
						{
							valid = false
						}
					}

					if (valid)
					{
						possibleDoorTiles.add(Point.obtain().set(tx, ty))
					}
				}
			}
			else
			{
				for (iy in 0..height - 1)
				{
					val tx = child2!!.x
					val ty = y + iy

					var valid = true
					if (valid)
					{
						val ttx = tx - 1
						val tty = ty
						if (ttx >= 0 && grid[ttx][tty] != 1)
						{
							valid = false
						}
					}

					if (valid)
					{
						val ttx = tx + 1
						val tty = ty
						if (ttx < gridWidth && grid[ttx][tty] != 1)
						{
							valid = false
						}
					}

					if (valid)
					{
						possibleDoorTiles.add(Point.obtain().set(tx, ty))
					}
				}
			}

			val doorPos = if (possibleDoorTiles.size > 0) possibleDoorTiles.ran(ran) else null

			if (doorPos != null)
			{
				grid[doorPos.x][doorPos.y] = 2
			}

			Point.freeAll(possibleDoorTiles)
		}

		fun dig(grid: Array<IntArray>, ran: Random)
		{
			if (child1 != null)
			{
				child1!!.dig(grid, ran)
				child2!!.dig(grid, ran)
				placeDoor(grid, ran)
			}
			else
			{
				for (ix in 1..width - 1)
				{
					for (iy in 1..height - 1)
					{
						grid[x + ix][y + iy] = 1
					}
				}
			}

		}

		companion object
		{

			private val minSize = 5
			private val maxSize = 12
		}
	}

	override fun process(grid: Array2D<Symbol>, symbolMap: ObjectMap<Char, Symbol>, ran: Random)
	{
		val width = grid.xSize
		val height = grid.ySize

		val wall = symbolMap.get('#')
		val floor = symbolMap.get('.')
		val door = symbolMap.get('+')
		var outGrid: Array<IntArray>? = null

		while (true)
		{
			val tree = BSPTree(0, 0, width - 1, height - 1)

			if (tree.child1 == null)
			{
				tree.partition(ran)
			}

			outGrid = Array(width) { IntArray(height) }
			tree.dig(outGrid, ran)

			if (isConnected(outGrid))
			{
				break
			}

			println("Failed to connect all chambers. Retrying")
		}

		for (x in 0..width - 1)
		{
			for (y in 0..height - 1)
			{
				if (outGrid!![x][y] == 0)
				{
					grid.array[x][y] = wall.copy()
				}
				else if (outGrid[x][y] == 1)
				{
					grid.array[x][y] = floor.copy()
				}
				else if (outGrid[x][y] == 2)
				{
					grid.array[x][y] = door.copy()
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private fun isConnected(grid: Array<IntArray>): Boolean
	{
		val width = grid.size
		val height = grid[0].size

		val reached = Array(width) { BooleanArray(height) }

		var x = 0
		var y = 0

		x = 0
		outer@ while (x < width)
		{
			y = 0
			while (y < height)
			{
				if (grid[x][y] >= 1)
				{
					break@outer
				}
				y++
			}
			x++
		}

		val toBeProcessed = com.badlogic.gdx.utils.Array<Point>()
		toBeProcessed.add(Point.obtain().set(x, y))

		while (toBeProcessed.size > 0)
		{
			val point = toBeProcessed.pop()
			x = point.x
			y = point.y
			point.free();

			if (reached[x][y])
			{
				continue
			}

			reached[x][y] = true

			for (dir in Direction.Values)
			{
				val nx = x + dir.x
				val ny = y + dir.y

				if (nx >= 0 && ny >= 0 && nx < width && ny < height && grid[nx][ny] >= 1)
				{
					toBeProcessed.add(Point.obtain().set(nx, ny))
				}
			}
		}

		x = 0
		while (x < width)
		{
			y = 0
			while (y < height)
			{
				if (grid[x][y] >= 1 && !reached[x][y])
				{
					return false
				}
				y++
			}
			x++
		}

		return true
	}

	override fun parse(xml: Element)
	{
	}
}
