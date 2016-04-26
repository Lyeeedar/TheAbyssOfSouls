package com.lyeeedar.DungeonGeneration.RoomGenerators

import java.util.Random

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Direction
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Point

/*
 * Builds a 'burrow' - that is the organic looking patterns
 * discovered by Kusigrosz and documented in this thread
 * http://groups.google.com/group/rec.games.roguelike.development/browse_thread/thread/4c56271970c253bf
 */

/* Arguments:
 *	 ngb_min, ngb_max: the minimum and maximum number of neighbouring
 *		 floor cells that a wall cell must have to become a floor cell.
 *		 1 <= ngb_min <= 3; ngb_min <= ngb_max <= 8;
 *	 connchance: the chance (in percent) that a new connection is
 *		 allowed; for ngb_max == 1 this has no effect as any
 *		 connecting cell must have 2 neighbours anyway.
 *	 cellnum: the maximum number of floor cells that will be generated.
 * The default values of the arguments are defined below.
 *
 * Algorithm description:
 * The algorithm operates on a rectangular grid. Each cell can be 'wall'
 * or 'floor'. A (non-border) cell has 8 neigbours - diagonals count.
 * There is also a cell store with two operations: store a given cell on
 * top, and pull a cell from the store. The cell to be pulled is selected
 * randomly from the store if N_cells_in_store < 125, and from the top
 * 25 * cube_root(N_cells_in_store) otherwise. There is no check for
 * repetitions, so a given cell can be stored multiple times.
 *
 * The algorithm starts with most of the map filled with 'wall', with a
 * "seed" of some floor cells; their neigbouring wall cells are in store.
 * The main loop in delveon() is repeated until the desired number of
 * floor cells is achieved, or there is nothing in store:
 *	 1) Get a cell from the store;
 *	 Check the conditions:
 *	 a) the cell has between ngb_min and ngb_max floor neighbours,
 *	 b) making it a floor cell won't open new connections,
 *		 or the RNG allows it with connchance/100 chance.
 *	 if a) and b) are met, the cell becomes floor, and its wall
 *	 neighbours are put in store in random order.
 * There are many variants possible, for example:
 * 1) picking the cell in rndpull() always from the whole store makes
 *	 compact patterns;
 * 2) storing the neighbours in digcell() clockwise starting from
 *	 a random one, and picking the bottom cell in rndpull() creates
 *	 meandering or spiral patterns.
 */
class Burrow : AbstractRoomGenerator(false)
{
	private var floorCoverage: Float = 0.toFloat()
	private var connectionChance: Float = 0.toFloat()

	private val tempArray = com.badlogic.gdx.utils.Array<Point>()

	private fun canPlace(grid: Array<Array<Symbol>>, floor: Symbol, wall: Symbol, ran: Random, p: Point): Boolean
	{
		return true
	}

	private fun getCellFromStore(cellStore: com.badlogic.gdx.utils.Array<Point>, ran: Random): Point
	{
		var range = cellStore.size
		if (cellStore.size > 125)
		{
			range = (25 * Math.pow(cellStore.size.toDouble(), (1.0f / 3.0f).toDouble())).toInt()
		}

		val index = ran.nextInt(range)
		val p = cellStore.removeIndex(cellStore.size - index - 1)

		return p
	}

	private fun addNeighboursToCellStore(grid: Array<Array<Symbol>>, wall: Symbol, point: Point, cellStore: com.badlogic.gdx.utils.Array<Point>)
	{
		for (dir in Direction.Values)
		{
			val nx = point.x + dir.x
			val ny = point.y + dir.y

			if (nx < 0 || ny < 0 || nx >= grid.size || ny >= grid[0].size)
			{
				continue
			}

			if (grid[nx][ny].contents.get(SpaceSlot.WALL) != null)
			{
				cellStore.add(Point.obtain().set(nx, ny))
			}
		}
	}

	private fun placeNewSeed(grid: Array<Array<Symbol>>, floor: Symbol, wall: Symbol, ran: Random): Point
	{
		val width = grid.size
		val height = grid[0].size

		for (x in 0..width - 1)
		{
			for (y in 0..height - 1)
			{
				if (grid[x][y] == wall)
				{
					val pos = Point.obtain().set(x, y)

					tempArray.add(pos)
				}
			}
		}

		val chosen = tempArray.get(ran.nextInt(tempArray.size)).copy()
		grid[chosen.x][chosen.y] = floor.copy()

		Point.freeAll(tempArray)
		tempArray.clear()

		return chosen
	}

	override fun process(grid: Array2D<Symbol>, symbolMap: ObjectMap<Char, Symbol>, ran: Random)
	{
		val cellStore = com.badlogic.gdx.utils.Array<Point>(false, 16)
		var placedCount = 0

		val width = grid.xSize
		val height = grid.ySize

		val wall = symbolMap.get('#')
		val floor = symbolMap.get('.')

		val targetTileCount = (width.toFloat() * height.toFloat() * floorCoverage).toInt()

		// Place seed tiles
		for (i in 0..7)
		{
			val seed = placeNewSeed(grid.array, floor, wall, ran)
			placedCount++
			addNeighboursToCellStore(grid.array, wall, seed, cellStore)
			seed.free()
		}

		// place tiles
		while (placedCount < targetTileCount)
		{
			if (cellStore.size == 0)
			{
				val seed = placeNewSeed(grid.array, floor, wall, ran)
				placedCount++
				addNeighboursToCellStore(grid.array, wall, seed, cellStore)
				seed.free()
			} else
			{
				val p = getCellFromStore(cellStore, ran)
				if (canPlace(grid.array, floor, wall, ran, p))
				{
					grid.array[p.x][p.y] = floor
					placedCount++
					addNeighboursToCellStore(grid.array, wall, p, cellStore)
					p.free()
				}
			}
		}
	}

	override fun parse(xml: Element)
	{
		floorCoverage = xml.getFloat("FloorCoverage", 0.6f)
		connectionChance = xml.getFloat("ConnectionChance", 0.2f)
	}

}
