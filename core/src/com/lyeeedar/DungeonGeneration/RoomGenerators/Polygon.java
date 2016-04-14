package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

public class Polygon extends AbstractRoomGenerator
{
	private static final int REGION = 25;

	public Polygon( )
	{
		super( false );
	}

	/*
	 * The following 3 functions (floodfill, floodall, joinall) are creditted to Ray Dillinger.
	 *
	 * These can be used to join disconnected regions.
	 */
	private void floodfill(int[][] map, int size_y, int size_x, int y, int x, int mark, int[] miny, int[] minx)
	{
		int i;
		int j;
		for (i=-1;i<=1;i++)
			for (j=-1;j<=1;j++)
				if (i+x < size_x && i+x >= 0 &&
				j+y < size_y && j+y >= 0 &&
				map[j+y][i+x] != 0 && map[j+y][i+x] != mark)
				{
					map[j+y][i+x] = mark;
					/* side effect of floodfilling is recording minimum x and y
						for each region*/
					if (mark < REGION)
					{
						if (i+x < minx[mark]) minx[mark] = i+x;
						if (j+y < miny[mark]) miny[mark] = j+y;
					}
					floodfill(map, size_y, size_x, j+y, i+x, mark, miny, minx);
				}

	}

	/* find all regions, mark each open cell (by floodfill) with an integer
		2 or greater indicating what region it's in. */
	private int floodall(int[][] map, int size_y, int size_x, int[] miny,int[] minx)
	{
		int x;
		int y;
		int count = 2;
		int retval = 0;
		/* start by setting all floor tiles to 1. */
		/* wall spaces are marked 0. */
		for (y=0;y< size_y;y++)
		{
			for (x=0;x< size_x;x++)
			{
				if (map[y][x] != 0)
				{
					map[y][x] = 1;
				}
			}
		}

		/* reset region extent marks to -1 invalid */
		for (x=0;x<REGION;x++)
		{
			minx[x] = -1;
			miny[x] = -1;
		}

		/* now mark regions starting with the number 2. */
		for (y=0;y< size_y;y++)
		{
			for (x=0;x< size_x;x++)
			{
				if (map[y][x] == 1)
				{
					if (count < REGION)
					{
						minx[count] = x;
						miny[count] = y;
					}
					floodfill(map, size_y, size_x, y, x, count, miny, minx);
					count++;
					retval++;
				}
			}
		}
		/* return the number of floodfill regions found */
		return(retval);

	}

	/*
	 * Instead of joining the regions, we remove all but the largest region, and renumber
	 * this largest region
	 */
	private int removeallbutlargest(int[][] map, int size_y, int size_x)
	{
		int[] count = new int[REGION];
		int y, x, c = 2;
		int retval;

		retval = floodall(map, size_y, size_x, count, count);

		/* if we have multiple unconnected regions */
		if (retval > 1)
		{
			for (x = 0; x < REGION; x++)
			{
				count[x]=0;
			}

			for (y = 0; y < size_y; y++)
				for (x = 0; x < size_x; x++)
				{
					count[map[y][x]]++;
				}

			c = 0;

			for (x = 0; x < REGION; x++)
			{
				if (count[x] > count[c]) c = x;
			}
		}

		/* Remove all but largest region */
		for (y = 0; y < size_y; y++)
			for (x = 0; x < size_x; x++)
				if (map[y][x] == c) map[y][x] = 1;
				else map[y][x] = 0;

		return(1);
	}


	/*
	 * The following function is from http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html#The%20C%20Code
	 *
	 * Copyright (c) 1970-2003, Wm. Randolph Franklin
	 *
	 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
	 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
	 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
	 * so, subject to the following conditions:
	 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
	 * 2. Redistributions in binary form must reproduce the above copyright notice in the documentation and/or other materials provided
	 * with the distribution.
	 * 3. The name of W. Randolph Franklin may not be used to endorse or promote products derived from this Software without specific
	 * prior written permission.
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
	 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 */
	private boolean pnpoly(int nvert, float[] vertx, float[] verty, float testx, float testy)
	{
		int i;
		int j;
		boolean c = false;

		for (i = 0, j = nvert-1; i < nvert; j = i++)
		{
			if ( ((verty[i]>testy) != (verty[j]>testy)) &&
					(testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
			{
				c = !c;
			}
		}
		return c;
	}


	/* It is possible to generate poly rooms with very small sizes. We ensure that these rooms have
	 * a minimum size */
	private final int MIN_POLY_ROOM_SIZE = 17;

	/*
	 * Generates a room from a convex or concave polygon. We don't completely handle the complex (self-intersecting) case
	 * but it could be extended to do so. Most of the smarts are on pnpoly (above).
	 *
	 * *y and *x represent pairs of vertex coordinates. These should probably be ordered but may not require this.
	 */
	private boolean generate_poly_room(int n, int[] y, int[] x, Symbol[][] grid, Symbol floor, Symbol wall)
	{
		float[] verty = new float[n+1];
		float[] vertx = new float[n+1];
		int i, xi, yi, size_y, size_x;
		int y0 = 256;
		int x0 = 256;
		int y1 = 0;
		int x1 = 0;
		int floors = 0;

		/* Copy contents to floating array */
		for (i = 0; i < n-1; i++)
		{
			verty[i] = (float)y[i];
			vertx[i] = (float)x[i];

			/* Determine extents */
			if (y[i] > y1) y1 = y[i];
			if (y[i] < y0) y0 = y[i];
			if (x[i] > x1) x1 = x[i];
			if (x[i] < x0) x0 = x[i];
		}

		/* Safety - repeat initial vertex at end of polygon */
		if ((y[n-1] != y[0]) || (x[n-1] != x[0]))
		{
			verty[n] = (float)y[0];
			vertx[n] = (float)x[0];
			n++;
		}

		/* Allocate space for the region borders */
		//y0--; x0--; y1++; x1++;

		/* Get grid size */
		size_y = y1 - y0 - 1;
		size_x = x1 - x0 - 1;

		int[][] tgrid = new int[size_y][size_x];

		/* Draw the polygon */
		for (yi = 0; yi < size_y; yi++)
		{
			for (xi = 0; xi < size_x; xi++)
			{
				/* Point in the polygon */
				if (pnpoly(n, verty, vertx, (float)(yi + y0), (float)(xi + x0)))
				{
					tgrid[yi][xi] = 0;
					floors++;
				}
				else
				{
					tgrid[yi][xi] = 1;
				}
			}
		}

		/* Ensure minimum size */
		if (floors < MIN_POLY_ROOM_SIZE) return false;

		/* Remove all but largest region */
		removeallbutlargest(tgrid, size_y, size_x);

		/* Write final grids out to map */
		for(yi=0; yi<size_y; yi++)
		{
			for(xi=0; xi<size_x; xi++)
			{
				if (tgrid[yi][xi] == 1)
				{
					grid[xi][yi] = wall;
				}
				else
				{
					grid[xi][yi] = floor;
				}
			}
		}

		return true;
	}

	/*
	 * Helper function for build_type_concave. We use this to generate
	 * inner concave spaces as well.
	 */

	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		int width = grid.getXSize();
		int height = grid.getYSize();

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );

		int[] verty = new int[12];
		int[] vertx = new int[12];
		int n = 0;
		int d, i;

		int points = 4;
		int y1a = 0;
		int x1a = 0;
		int y2a = 0;
		int x2a = width;
		int y1b = height;
		int x1b = 0;
		int y2b = height;
		int x2b = width;

		/* Generate 1 or more points for each edge */
		d = ran.nextInt(points-1)+1;

		/* Add vertices */
		for (i = 0; i < d; i++)
		{
			int y1w = (x1a < x1b ? y1a : (x1a == x1b ? Math.min(y1a, y1b) : y1b));
			int y2w = (x1a < x1b ? y2a : (x1a == x1b ? Math.max(y2a, y2b) : y2b));
			int x1w = Math.min(x1a, x1b);
			int x2w = (x1a == x1b ? x1a + 1 : Math.max(x1a, x1b) - 1);

			int y = y1w + ran.nextInt(y2w - y1w + 1);
			int x = x1w + ran.nextInt(x2w - x1w + 1);

			int j = n;

			/* Sort from highest to lowest y */
			while ((j > 0) && (verty[j-1] <= y))
			{
				verty[j] = verty[j-1];
				vertx[j] = vertx[j-1];
				j--;
			}

			/* Insert new value */
			verty[j] = y;
			vertx[j] = x;
			n++;
		}

		/* Generate 1 or more points for each edge */
		d = ran.nextInt(points-1)+1;

		/* Add vertices */
		for (i = 0; i < d; i++)
		{
			int y1n = Math.min(y1a, y1b);
			int y2n = (y1a == y1b ? y1a + 1 : Math.max(y1a, y1b) - 1);
			int x1n = (y1a < y1b ? x1a : (y1a == y1b ? Math.min(x1a, x1b): x1b));
			int x2n = (y1a < y1b ? x2a : (y1a == y1b ? Math.max(x2a, x2b): x2b));

			int y = y1n + ran.nextInt(y2n - y1n + 1);
			int x = x1n + ran.nextInt(x2n - x1n + 1);

			int j = n;

			/* Sort from lowest to highest x */
			while ((j > 0) && (vertx[j-1] >= x))
			{
				verty[j] = verty[j-1];
				vertx[j] = vertx[j-1];
				j--;
			}

			/* Insert new value */
			verty[j] = y;
			vertx[j] = x;
			n++;
		}

		/* Generate 1 or more points for each edge */
		d = ran.nextInt(points-1)+1;

		/* Add vertices */
		for (i = 0; i < d; i++)
		{
			int y1e = (x2a > x2b ? y1a : (x1a == x1b ? Math.min(y1a, y1b): y1b));
			int y2e = (x2a > x2b ? y2a : (x1a == x1b ? Math.max(y2a, y2b): y2b));
			int x1e = (x2a == x2b ? x2a - 1 : Math.min(x2a, x2b) + 1);
			int x2e = Math.max(x2a, x2b);

			int y = y1e + ran.nextInt(y2e - y1e + 1);
			int x = x1e + ran.nextInt(x2e - x1e + 1);

			int j = n;

			/* Sort from lowest to highest y */
			while ((j > 0) && (verty[j-1] >= y))
			{
				verty[j] = verty[j-1];
				vertx[j] = vertx[j-1];
				j--;
			}

			/* Insert new value */
			verty[j] = y;
			vertx[j] = x;
			n++;
		}

		/* Generate 1 or more points for each edge */
		d = ran.nextInt(points-1)+1;

		/* Add vertices */
		for (i = 0; i < d; i++)
		{
			int y1s = (y2a == y2b ? y2a - 1 : Math.min(y2a, y2b) + 1);
			int y2s = Math.max(y2a, y2b);
			int x1s = (y2a > y2b ? x1a : (y2a == y2b ? Math.min(x1a, x1b): x1b));
			int x2s = (y2a > y2b ? x2a : (y2a == y2b ? Math.max(x2a, x2b): x2b));

			int y = y1s + ran.nextInt(y2s - y1s + 1);
			int x = x1s + ran.nextInt(x2s - x1s + 1);

			int j = n;

			/* Sort from highest to lowest x */
			while ((j > 0) && (vertx[j-1] <= x))
			{
				verty[j] = verty[j-1];
				vertx[j] = vertx[j-1];
				j--;
			}

			/* Insert new value */
			verty[j] = y;
			vertx[j] = x;
			n++;
		}

		/* Remove duplicate vertices */
		for (i = 1; i < n; i++)
		{
			if ((verty[i] == verty[i-1]) && (vertx[i] == vertx[i-1]))
			{
				int j;

				for (j = i + 1; j < n; j++)
				{
					verty[j-1] = verty[j];
					vertx[j-1] = vertx[j];
				}

				n--;
			}
		}

		/* Generate the polygon */
		generate_poly_room(n, verty, vertx, grid.getArray(), floor, wall);
	}



	@Override
	public void parse(Element xml)
	{
	}


}
