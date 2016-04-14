package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import com.lyeeedar.Enums;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

/**
 * Seperates the room via Binary Space partitioning, then places doors to
 * connect the branches of the tree.
 *
 * @author Philip Collin
 *
 */
public class Chambers extends AbstractRoomGenerator
{
	public Chambers()
	{
		super( true );
	}

	public static class BSPTree
	{
		public int x;
		public int y;
		public int width;
		public int height;

		public BSPTree( int x, int y, int width, int height )
		{
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public BSPTree child1;
		public BSPTree child2;
		public boolean splitVertically;

		private static final int minSize = 5;
		private static final int maxSize = 12;

		public void partition( Random ran )
		{
			if ( ( width < minSize * 2 && height < minSize * 2 ) || ( width < maxSize && height < maxSize && ran.nextInt( 5 ) == 0 ) )
			{

			}
			else if ( width < minSize * 2 )
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitheight = (int) ( height * split );

				child1 = new BSPTree( x, y, width, splitheight );
				child2 = new BSPTree( x, y + splitheight, width, height - splitheight );

				splitVertically = true;

				child1.partition( ran );
				child2.partition( ran );

			}
			else if ( height < minSize * 2 )
			{
				float split = 0.3f + ran.nextFloat() * 0.4f;
				int splitwidth = (int) ( width * split );

				child1 = new BSPTree( x, y, splitwidth, height );
				child2 = new BSPTree( x + splitwidth, y, width - splitwidth, height );

				splitVertically = false;

				child1.partition( ran );
				child2.partition( ran );
			}
			else
			{
				boolean vertical = ran.nextBoolean();
				if ( vertical )
				{
					float split = 0.3f + ran.nextFloat() * 0.4f;
					int splitwidth = (int) ( width * split );

					child1 = new BSPTree( x, y, splitwidth, height );
					child2 = new BSPTree( x + splitwidth, y, width - splitwidth, height );

					splitVertically = false;

					child1.partition( ran );
					child2.partition( ran );
				}
				else
				{
					float split = 0.3f + ran.nextFloat() * 0.4f;
					int splitheight = (int) ( height * split );

					child1 = new BSPTree( x, y, width, splitheight );
					child2 = new BSPTree( x, y + splitheight, width, height - splitheight );

					splitVertically = true;

					child1.partition( ran );
					child2.partition( ran );
				}
			}
		}

		private void placeDoor( int[][] grid, Random ran )
		{
			int gridWidth = grid.length;
			int gridHeight = grid[0].length;
			Array<int[]> possibleDoorTiles = new Array<int[]>();

			if ( splitVertically )
			{
				for ( int ix = 0; ix < width; ix++ )
				{
					int tx = x + ix;
					int ty = child2.y;

					boolean valid = true;
					if ( valid )
					{
						int ttx = tx;
						int tty = ty - 1;
						if ( tty >= 0 && grid[ttx][tty] != 1 )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						int ttx = tx;
						int tty = ty + 1;
						if ( tty < gridHeight && grid[ttx][tty] != 1 )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						possibleDoorTiles.add( new int[] { tx, ty } );
					}
				}
			}
			else
			{
				for ( int iy = 0; iy < height; iy++ )
				{
					int tx = child2.x;
					int ty = y + iy;

					boolean valid = true;
					if ( valid )
					{
						int ttx = tx - 1;
						int tty = ty;
						if ( ttx >= 0 && grid[ttx][tty] != 1 )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						int ttx = tx + 1;
						int tty = ty;
						if ( ttx < gridWidth && grid[ttx][tty] != 1 )
						{
							valid = false;
						}
					}

					if ( valid )
					{
						possibleDoorTiles.add( new int[] { tx, ty } );
					}
				}
			}

			int[] doorPos = possibleDoorTiles.size > 0 ? possibleDoorTiles.removeIndex( ran.nextInt( possibleDoorTiles.size ) ) : null;

			if ( doorPos != null )
			{
				grid[doorPos[0]][doorPos[1]] = 2;
			}

		}

		public void dig( int[][] grid, Random ran )
		{
			if ( child1 != null )
			{
				child1.dig( grid, ran );
				child2.dig( grid, ran );
				placeDoor( grid, ran );
			}
			else
			{
				for ( int ix = 1; ix < width; ix++ )
				{
					for ( int iy = 1; iy < height; iy++ )
					{
						grid[x + ix][y + iy] = 1;
					}
				}
			}

		}
	}

	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		int width = grid.getXSize();
		int height = grid.getYSize();

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );
		Symbol door = symbolMap.get( '+' );
		int[][] outGrid = null;

		while ( true )
		{
			BSPTree tree = new BSPTree( 0, 0, width - 1, height - 1 );

			if (tree.child1 == null)
			{
				tree.partition( ran );
			}

			outGrid = new int[width][height];
			tree.dig( outGrid, ran );

			if ( isConnected( outGrid ) )
			{
				break;
			}

			System.out.println( "Failed to connect all chambers. Retrying" );
		}

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( outGrid[x][y] == 0 )
				{
					grid.getArray()[x][y] = wall.copy();
				}
				else if ( outGrid[x][y] == 1 )
				{
					grid.getArray()[x][y] = floor.copy();
				}
				else if ( outGrid[x][y] == 2 )
				{
					grid.getArray()[x][y] = door.copy();
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	private boolean isConnected( int[][] grid )
	{
		int width = grid.length;
		int height = grid[0].length;

		boolean[][] reached = new boolean[width][height];

		int x = 0;
		int y = 0;

		outer:
			for ( x = 0; x < width; x++ )
			{
				for ( y = 0; y < height; y++ )
				{
					if ( grid[x][y] >= 1 )
					{
						break outer;
					}
				}
			}

		Array<int[]> toBeProcessed = new Array<int[]>();
		toBeProcessed.add( new int[] { x, y } );

		while ( toBeProcessed.size > 0 )
		{
			int[] point = toBeProcessed.pop();
			x = point[0];
			y = point[1];

			if ( reached[x][y] )
			{
				continue;
			}

			reached[x][y] = true;

			for ( Enums.Direction dir : Enums.Direction.Values )
			{
				int nx = x + dir.x;
				int ny = y + dir.y;

				if ( nx >= 0 && ny >= 0 && nx < width && ny < height && grid[nx][ny] >= 1 )
				{
					toBeProcessed.add( new int[] { nx, ny } );
				}
			}
		}

		for ( x = 0; x < width; x++ )
		{
			for ( y = 0; y < height; y++ )
			{
				if ( grid[x][y] >= 1 && !reached[x][y] ) { return false; }
			}
		}

		return true;
	}

	@Override
	public void parse( Element xml )
	{
	}
}
