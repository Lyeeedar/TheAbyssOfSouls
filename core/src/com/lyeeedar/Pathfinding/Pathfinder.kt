package com.lyeeedar.Pathfinding

import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

class Pathfinder<T: PathfindingTile>(private val Grid: Array<Array<T>>, private val startx: Int, private val starty: Int, private val endx: Int, private val endy: Int, private val canMoveDiagonal: Boolean, private val size: Int, private val self: Any)
{
	fun getPath(travelType: SpaceSlot): com.badlogic.gdx.utils.Array<Point>?
	{
		val astar = AStarPathfind(Grid, startx, starty, endx, endy, canMoveDiagonal, false, size, travelType, self)
		var path: com.badlogic.gdx.utils.Array<Point>? = astar.path

		if (path == null)
		{
			if (Global.canMoveDiagonal)
			{
				path = BresenhamLine.line(startx, starty, endx, endy, Grid, true, Integer.MAX_VALUE, travelType, self)
			} else
			{
				path = BresenhamLine.lineNoDiag(startx, starty, endx, endy, Grid, true, Integer.MAX_VALUE, travelType, self)
			}
		}

		return path
	}

	object PathfinderTest
	{
		class TestTile : PathfindingTile
		{
			var isPath = false
			var passable = true

			override fun getPassable(travelType: SpaceSlot, self: Any?): Boolean
			{
				return true
			}

			override fun getInfluence(travelType: SpaceSlot, self: Any?): Int
			{
				return if (passable) 0 else 1000
			}
		}

		fun runTests()
		{
			bucketTest()
			straightTest()
			wallTest()
			irregularTest()

		}

		private fun straightTest()
		{
			val sgrid = arrayOf("..........", "..........", "..........", "..........", "..........", "..........", "..........", "..........", "..........", "..........")

			runTest(sgrid)
		}

		private fun wallTest()
		{
			val sgrid = arrayOf("..........", "..........", "..........", "..........", ".########.", "..........", "..........", "..........", "..........", "..........")

			runTest(sgrid)
		}

		private fun irregularTest()
		{
			val sgrid = arrayOf(".......###", "..........", "#####.....", "..........", ".########.", "..........", "#..##.....", ".....#####", "..........", "..........")

			runTest(sgrid)
		}

		private fun bucketTest()
		{
			val sgrid = arrayOf("..........", "..........", ".#......#.", ".#......#.", ".########.", ".#......#.", ".#......#.", "##......#.", "..........", "..........")

			runTest(sgrid)
		}

		private fun runTest(grid: Array<String>)
		{
			val width = grid[0].length
			val height = grid.size

			val testgrid = Array(height) { arrayOfNulls<TestTile>(width) }
			for (x in 0..width - 1)
			{
				for (y in 0..height - 1)
				{
					testgrid[y][x] = TestTile()

					if (grid[y][x] == '#')
					{
						testgrid[y][x]?.passable = false
					}
				}
			}

			// diagonal
			path(testgrid, 1, 1, 8, 8)
			path(testgrid, 1, 8, 8, 1)

			// straight
			path(testgrid, 1, 1, 1, 8)
			path(testgrid, 1, 8, 8, 8)

			// offset
			path(testgrid, 1, 1, 2, 8)
		}

		private fun path(grid: Array<Array<TestTile?>>, startx: Int, starty: Int, endx: Int, endy: Int)
		{
			val astar = AStarPathfind(grid, startx, starty, endx, endy, false, true, 1, SpaceSlot.ENTITY, null)
			val path = astar.path

			for (step in path)
			{
				grid[step.x][step.y]?.isPath = true
			}

			for (x in 0..9)
			{
				for (y in 0..9)
				{
					var c = '.'
					if (grid[x][y]?.isPath ?: false)
					{
						c = 'p'
					} else if (!(grid[x][y]?.passable ?: false))
					{
						c = '#'
					}

					print("" + c)
				}

				print("\n")
			}

			print("\n")

			for (x in 0..9)
			{
				for (y in 0..9)
				{
					grid[x][y]?.isPath = false
				}
			}
		}
	}
}
