package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.Enums;
import com.lyeeedar.Util.Array2D;
import com.lyeeedar.Util.Point;
import org.jetbrains.annotations.NotNull;

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
public class Burrow extends AbstractRoomGenerator
{
	private float floorCoverage;
	private float connectionChance;

	private Array<Point> tempArray = new Array<Point>();

	public Burrow( )
	{
		super( false );
	}

	private boolean canPlace( Symbol[][] grid, Symbol floor, Symbol wall, Random ran, Point p )
	{
		return true;
	}

	private Point getCellFromStore( Array<Point> cellStore, Random ran )
	{
		int range = cellStore.size;
		if ( cellStore.size > 125 )
		{
			range = (int) ( 25 * Math.pow( cellStore.size, 1.0f / 3.0f ) );
		}

		int index = ran.nextInt( range );
		Point p = cellStore.removeIndex( cellStore.size - index - 1 );

		return p;
	}

	private void addNeighboursToCellStore( Symbol[][] grid, Symbol wall, Point point, Array<Point> cellStore )
	{
		for ( Enums.Direction dir : Enums.Direction.Values )
		{
			int nx = point.x + dir.x;
			int ny = point.y + dir.y;

			if ( nx < 0 || ny < 0 || nx >= grid.length || ny >= grid[0].length )
			{
				continue;
			}

			if ( grid[nx][ny].getContents().get( Enums.SpaceSlot.WALL ) != null )
			{
				cellStore.add( Point.obtain().set( nx, ny ) );
			}
		}
	}

	private Point placeNewSeed( Symbol[][] grid, Symbol floor, Symbol wall, Random ran )
	{
		int width = grid.length;
		int height = grid[0].length;

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( grid[x][y] == wall )
				{
					Point pos = Point.obtain().set( x, y );

					tempArray.add( pos );
				}
			}
		}

		Point chosen = tempArray.get( ran.nextInt( tempArray.size ) ).copy();
		grid[chosen.x][chosen.y] = floor.copy();

		Point.freeAll( tempArray );
		tempArray.clear();

		return chosen;
	}

	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		Array<Point> cellStore = new Array<Point>( false, 16 );
		int placedCount = 0;

		int width = grid.getXSize();
		int height = grid.getYSize();

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );

		int targetTileCount = (int) ( width * height * floorCoverage );

		// Place seed tiles
		for ( int i = 0; i < 8; i++ )
		{
			Point seed = placeNewSeed( grid.getArray(), floor, wall, ran );
			placedCount++;
			addNeighboursToCellStore( grid.getArray(), wall, seed, cellStore );
			seed.free();
		}

		// place tiles
		while ( placedCount < targetTileCount )
		{
			if ( cellStore.size == 0 )
			{
				Point seed = placeNewSeed( grid.getArray(), floor, wall, ran );
				placedCount++;
				addNeighboursToCellStore( grid.getArray(), wall, seed, cellStore );
				seed.free();
			}
			else
			{
				Point p = getCellFromStore( cellStore, ran );
				if ( canPlace( grid.getArray(), floor, wall, ran, p ) )
				{
					grid.getArray()[p.x][p.y] = floor;
					placedCount++;
					addNeighboursToCellStore( grid.getArray(), wall, p, cellStore );
					p.free();
				}
			}
		}
	}

	@Override
	public void parse( Element xml )
	{
		floorCoverage = xml.getFloat( "FloorCoverage", 0.6f );
		connectionChance = xml.getFloat( "ConnectionChance", 0.2f );
	}

}
