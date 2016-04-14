package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

public class MidpointDisplacement
{
	public int width, height;

	private final Random ran;

	public MidpointDisplacement( Random ran, int width, int height )
	{
		this.ran = ran;
		this.width = width;
		this.height = height;
	}

	private float[][] map;

	// This is the actual mid point displacement code.
	public void midpoint(int x1,int y1, int x2, int y2 ){

		if ( x2 - x1 < 2 && y2 - y1 < 2) { return; }

		// Find distance between points and
		// use when generating a random number.
		int dist = x2 - x1 + y2 - y1;
		int hdist = dist / 2;
		// Find Middle Point
		int midx = ( x1 + x2 ) / 2;
		int midy = ( y1 + y2 ) / 2;
		// Get vals of corners
		float c1 = map[x1][y1];
		float c2 = map[x2][y1];
		float c3 = map[x2][y2];
		float c4 = map[x1][y2];

		// If Not already defined, work out the midpoints of the corners of
		// the rectangle by means of an average plus a random number.
		if ( map[midx][y1] == -1 ) { map[midx][y1] = Math.max( 0, ( c1 + c2 + ran.nextInt(dist) - hdist ) / 2 ); }
		if ( map[midx][y2] == -1 ) { map[midx][y2] = Math.max( 0, ( c4 + c3 + ran.nextInt(dist) - hdist ) / 2 ); }
		if ( map[x1][midy] == -1 ) { map[x1][midy] = Math.max( 0, ( c1 + c4 + ran.nextInt(dist) - hdist ) / 2 ); }
		if ( map[x2][midy] == -1 ) { map[x2][midy] = Math.max( 0, ( c2 + c3 + ran.nextInt(dist) - hdist ) / 2 ); }

		// Work out the middle point...
		if ( map[midx][midy] == -1 ) { map[midx][midy] = Math.max( 0, ( c1 + c2 + c3 + c4 + ran.nextInt(dist) - hdist ) / 4 ); }

		// Now divide this rectangle into 4, And call again For Each smaller
		// rectangle
		midpoint( x1, y1, midx, midy );
		midpoint( midx, y1, x2, midy );
		midpoint( x1, midy, midx, y2 );
		midpoint( midx, midy, x2, y2 );
	}

	public float[][] getMap()
	{
		// initialize arrays to hold values
		map = new float[width][height];

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				if ( x == 0 || y == 0 || x == width - 1 || y == height - 1 )
				{
					map[x][y] = 0;
				}
				else
				{
					map[x][y] = -1;
				}
			}
		}

		midpoint( 0, 0, width-1, height-1 );

		// Normalize the map
		float max = Float.MIN_VALUE;
		float min = Float.MAX_VALUE;
		for ( float[] row : map )
		{
			for ( float d : row )
			{
				if ( d > max ) max = d;
				if ( d < min ) min = d;
			}
		}

		for ( int x = 0; x < width; x++ )
		{
			for (int y = 0; y < height; y++)
			{
				float val = (map[x][y] - min) / (max - min);

				map[x][y] = val;

				System.out.print(val + " ");
			}
			System.out.print( "\n" );
		}

		return map;
	}
}
