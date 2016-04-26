package com.lyeeedar.Pathfinding;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.lyeeedar.SpaceSlot;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;

public class BresenhamLine
{
	public static Array<Point> lineNoDiag( int x0, int y0, int x1, int y1 )
	{
		return lineNoDiag( x0, y0, x1, y1, null, false, 0, null, null );
	}

	public static Array<Point> lineNoDiag( int x0, int y0, int x1, int y1, PathfindingTile[][] Grid, boolean checkPassable, int range, SpaceSlot travelType, Object self )
	{
		int xDist = Math.abs( x1 - x0 );
		int yDist = -Math.abs( y1 - y0 );
		int xStep = ( x0 < x1 ? +1 : -1 );
		int yStep = ( y0 < y1 ? +1 : -1 );
		int error = xDist + yDist;

		Array<Point> path = new Array<Point>();

		path.add( Point.obtain().set( x0, y0 ) );

		while ( x0 != x1 || y0 != y1 )
		{
			if ( 2 * error - yDist > xDist - 2 * error )
			{
				// horizontal step
				error += yDist;
				x0 += xStep;
			}
			else
			{
				// vertical step
				error += xDist;
				y0 += yStep;
			}

			if ( Grid != null && (x0 < 0 || y0 < 0 || x0 >= Grid.length - 1 || y0 >= Grid[0].length - 1 || ( checkPassable && !Grid[x0][y0].getPassable( travelType, self ) ) ) )
			{
				break;
			}

			path.add( Point.obtain().set( x0, y0 ) );
		}

		return path;
	}

	public static Array<Point> line( int x, int y, int x2, int y2, PathfindingTile[][] Grid, boolean checkPassable, int range, SpaceSlot travelType, Object self )
	{
		x = MathUtils.clamp( x, 0, Grid.length - 1 );
		x2 = MathUtils.clamp( x2, 0, Grid.length - 1 );
		y = MathUtils.clamp( y, 0, Grid[0].length - 1 );
		y2 = MathUtils.clamp( y2, 0, Grid[0].length - 1 );

		if ( x == x2 && y == y2 ) { return null; }

		int w = x2 - x;
		int h = y2 - y;

		int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

		if ( w < 0 ) dx1 = -1;
		else if ( w > 0 ) dx1 = 1;
		if ( h < 0 ) dy1 = -1;
		else if ( h > 0 ) dy1 = 1;
		if ( w < 0 ) dx2 = -1;
		else if ( w > 0 ) dx2 = 1;

		int longest = Math.abs( w );
		int shortest = Math.abs( h );

		if ( !( longest > shortest ) )
		{
			longest = Math.abs( h );
			shortest = Math.abs( w );

			if ( h < 0 ) dy2 = -1;
			else if ( h > 0 ) dy2 = 1;
			dx2 = 0;
		}

		int numerator = longest >> 1;

		int dist = range;

		Array<Point> path = new Array<Point>();

		for ( int i = 0; i <= dist; i++ )
		{
			path.add( Point.obtain().set( x, y ) );

			numerator += shortest;
			if ( !( numerator < longest ) )
			{
				numerator -= longest;
				x += dx1;
				y += dy1;
			}
			else
			{
				x += dx2;
				y += dy2;
			}

			if ( x < 0 || y < 0 || x >= Grid.length - 1 || y >= Grid[0].length - 1 || ( checkPassable && !Grid[x][y].getPassable( travelType, self ) ) )
			{
				break;
			}
		}

		return path;
	}
}
