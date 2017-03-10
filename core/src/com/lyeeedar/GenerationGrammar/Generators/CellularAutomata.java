package com.lyeeedar.GenerationGrammar.Generators;

import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.GenerationGrammar.Area;
import com.lyeeedar.GenerationGrammar.GrammarSymbol;
import squidpony.squidmath.LightRNG;

public class CellularAutomata extends AbstractRoomGenerator
{
	private static final int GRID_WALL = 0;
	private static final int GRID_FLOOR = 1;

	public int[][] joinRegions( int[][] igrid, int[][] igrid2 )
	{
		for ( int x = 0; x < igrid.length; x++ )
		{
			for ( int y = 0; y < igrid[0].length; y++ )
			{
				igrid[x][y] = igrid[x][y] == 1 || igrid2[x][y] == 1 ? 1 : 0;
			}
		}

		return igrid;
	}

	/*
	 * This builds a cave-like region using cellular automata identical to that
	 * outlined in the algorithm at
	 * http://roguebasin.roguelikedevelopment.org/index
	 * .php?title=Cellular_Automata_Method_for_Generating_Random_Cave
	 * -Like_Levels
	 *
	 * The floor and wall grid define which features are generated. Wall_prob
	 * defines the chance of being initialised as a wall grid.
	 *
	 * R1 defines the minimum number of walls that must be within 1 grid to make
	 * the new grid a wall R2 defines the maximum number of walls that must be
	 * within 2 grids to make the new grid a wall else the new grid is a floor
	 * gen gives the number of generations. gen2 gives the number of generations
	 * from which the r2 parameter is ignored
	 *
	 * Examples given in the article are: wall_prob = 45, r1 = 5, r2 = N/A, gen
	 * = 5, gen2 = 0 wall_prob = 45, r1 = 5, r2 = 0, gen = 5, gen2 = 5 wall_prob
	 * = 40, r1 = 5, r2 = 2, gen = 7, gen2 = 4
	 *
	 * We can define a combination of wall, floor and edge to allow e.g. a
	 * series of islands rising out of lava or some other mix of terrain.
	 */
	@Override
	public void process( Area area, GrammarSymbol floor, GrammarSymbol wall, LightRNG ran )
	{
		int wall_prob = ran.nextInt( 5 ) + 40;
		int r1 = 5;
		int r2 = ran.nextInt( 2 );
		int gen = ran.nextInt( 2 ) + 5;
		int gen2 = ran.nextInt( 5 );

		int xi, yi;

		int size_y = area.getHeight();
		int size_x = area.getWidth();

		int ii, jj;

		int[][] igrid = new int[size_y][size_x];
		int[][] igrid2 = new int[size_y][size_x];

		/* Initialise the starting grids randomly */
		for ( yi = 1; yi < size_y - 1; yi++ )
		{
			for ( xi = 1; xi < size_x - 1; xi++ )
			{
				igrid[yi][xi] = ran.nextInt( 100 ) < wall_prob ? GRID_WALL : GRID_FLOOR;
			}
		}

		/* Initialise the destination grids - for paranoia */
		for ( yi = 0; yi < size_y; yi++ )
		{
			for ( xi = 0; xi < size_x; xi++ )
			{
				igrid2[yi][xi] = GRID_WALL;
			}
		}

		/* Surround the starting grids in walls */
		for ( yi = 0; yi < size_y; yi++ )
		{
			igrid[yi][0] = igrid[yi][size_x - 1] = GRID_WALL;
		}
		for ( xi = 0; xi < size_x; xi++ )
		{
			igrid[0][xi] = igrid[size_y - 1][xi] = GRID_WALL;
		}

		/* Run through generations */
		for ( ; gen > 0; gen--, gen2-- )
		{
			for ( yi = 1; yi < size_y - 1; yi++ )
			{
				for ( xi = 1; xi < size_x - 1; xi++ )
				{
					int adjcount_r1 = 0, adjcount_r2 = 0;

					// Count adjacent
					for ( ii = -1; ii <= 1; ii++ )
					{
						for ( jj = -1; jj <= 1; jj++ )
						{
							if ( igrid[yi + ii][xi + jj] != GRID_FLOOR )
							{
								adjcount_r1++;
							}
						}
					}

					for ( ii = yi - 2; ii <= yi + 2; ii++ )
					{
						for ( jj = xi - 2; jj <= xi + 2; jj++ )
						{
							if ( Math.abs( ii - yi ) == 2 && Math.abs( jj - xi ) == 2 )
							{
								continue;
							}
							if ( ii < 0 || jj < 0 || ii >= size_y || jj >= size_x )
							{
								continue;
							}
							if ( igrid[ii][jj] != GRID_FLOOR )
							{
								adjcount_r2++;
							}
						}
					}
					if ( adjcount_r1 >= r1 || ( ( gen2 > 0 ) && ( adjcount_r2 <= r2 ) ) )
					{
						igrid2[yi][xi] = GRID_WALL;
					}
					else
					{
						igrid2[yi][xi] = GRID_FLOOR;
					}
				}
			}
			for ( yi = 1; yi < size_y - 1; yi++ )
			{
				for ( xi = 1; xi < size_x - 1; xi++ )
				{
					igrid[yi][xi] = igrid2[yi][xi];
				}
			}
		}

		igrid = joinRegions( igrid, igrid2 );

		/* Write final grids out to map */
		for ( yi = 0; yi < size_y; yi++ )
		{
			for ( xi = 0; xi < size_x; xi++ )
			{
				if ( igrid[yi][xi] == GRID_FLOOR )
				{
					area.get( xi, yi ).write( floor, false );
					//grid[xi][yi] = floor;
				}
				else
				{
					area.get( xi, yi ).write( wall, false );
					//grid[xi][yi] = wall;
				}
			}
		}
	}

	@Override
	public void parse( Element xml )
	{
	}
}
