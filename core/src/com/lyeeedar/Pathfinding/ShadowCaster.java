/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * <p>
 * Contributors:
 * Philip Collin - initial API and implementation
 ******************************************************************************/
package com.lyeeedar.Pathfinding;

import java.util.ArrayDeque;
import java.util.HashSet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ObjectSet;
import com.lyeeedar.Enums;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;

public class ShadowCaster
{
	private final Enums.SpaceSlot ShadowPassability = Enums.SpaceSlot.WALL;

	private final IntSet tileLookup = new IntSet();

	private final int range;
	private final PathfindingTile[][] grid;
	private final Enums.SpaceSlot travelType;
	private final Object self;

	public boolean allowOutOfBounds = false;

	private int startX;
	private int startY;

	public ShadowCaster( PathfindingTile[][] grid, int range )
	{
		this.grid = grid;
		this.range = range;
		this.travelType = ShadowPassability;
		this.self = null;
	}

	public ShadowCaster( PathfindingTile[][] grid, int range, Enums.SpaceSlot travelType, Object self )
	{
		this.grid = grid;
		this.range = range;
		this.travelType = travelType;
		this.self = self;
	}

	// Takes a circle in the form of a center point and radius, and a function
	// that can tell whether a given cell is opaque. Calls the setFoV action on
	// every cell that is both within the radius and visible from the center.

	public void ComputeFOV( int x, int y, Array<Point> output )
	{
		this.startX = MathUtils.clamp( x, 0, grid.length - 1 );
		this.startY = MathUtils.clamp( y, 0, grid[ 0 ].length - 1 );

		for ( int octant = 0; octant < 8; octant++ )
		{
			ComputeFieldOfViewInOctantZero( octant, output );
		}
	}

	private void ComputeFieldOfViewInOctantZero( int octant, Array<Point> output )
	{
		ArrayDeque<Column> queue = new ArrayDeque<Column>();

		queue.addFirst( new Column( 0, new int[]{ 1, 0 }, new int[]{ 1, 1 }, octant ) );

		while ( !queue.isEmpty() )
		{
			Column current = queue.pollLast();
			if ( current.getX() > range )
			{
				continue;
			}

			ComputeFoVForColumnPortion( current.getX(), current.getTopVector(), current.getBottomVector(), queue, current.getOctant(), output );
		}
	}

	// This method has two main purposes: (1) it marks points inside the
	// portion that are within the radius as in the field of view, and
	// (2) it computes which portions of the following column are in the
	// field of view, and puts them on a work queue for later processing.
	private void ComputeFoVForColumnPortion( int x, int[] topVector, int[] bottomVector, ArrayDeque<Column> queue, int octant, Array<Point> output )
	{
		// Search for transitions from opaque to transparent or
		// transparent to opaque and use those to determine what
		// portions of the *next* column are visible from the origin.

		// Start at the top of the column portion and work down.

		int topY;
		if ( x == 0 )
		{
			topY = 0;
		}
		else
		{
			int quotient = ( 2 * x + 1 ) * topVector[ 1 ] / ( 2 * topVector[ 0 ] );
			int remainder = ( 2 * x + 1 ) * topVector[ 1 ] % ( 2 * topVector[ 0 ] );

			if ( remainder > topVector[ 0 ] )
			{
				topY = quotient + 1;
			}
			else
			{
				topY = quotient;
			}
		}

		// Note that this can find a top cell that is actually entirely blocked
		// by the cell below it; consider detecting and eliminating that.

		int bottomY;
		if ( x == 0 )
		{
			bottomY = 0;
		}
		else
		{
			int quotient = ( 2 * x - 1 ) * bottomVector[ 1 ] / ( 2 * bottomVector[ 0 ] );
			int remainder = ( 2 * x - 1 ) * bottomVector[ 1 ] % ( 2 * bottomVector[ 0 ] );

			if ( remainder >= bottomVector[ 0 ] )
			{
				bottomY = quotient + 1;
			}
			else
			{
				bottomY = quotient;
			}
		}

		// A more sophisticated algorithm would say that a cell is visible if
		// there is *any* straight line segment that passes through *any*
		// portion
		// of the origin cell and any portion of the target cell, passing
		// through
		// only transparent cells along the way. This is the "Permissive Field
		// Of
		// View" algorithm, and it is much harder to implement.
		Boolean wasLastCellOpaque = null;
		for ( int y = topY; y >= bottomY; y-- )
		{
			Point temp = Point.obtain();
			Point translated = TranslateOctant( temp.set( x, y ), octant );
			temp.free();

			boolean inRadius = IsInRadius( translated.x, translated.y );
			if ( inRadius )
			{
				// The current cell is in the field of view.

				if ( !allowOutOfBounds && ( translated.x < 0 || translated.y < 0 || translated.x >= grid.length || translated.y >= grid[ 0 ].length ) )
				{
					translated.free();
					continue;
				}

				int tileVal = translated.y * grid.length + translated.x;
				if ( !tileLookup.contains( tileVal ) )
				{
					output.add( translated );
					tileLookup.add( tileVal );
				}
				else
				{
					translated.free();
				}
			}
			else
			{
				translated.free();
			}

			// A cell that was too far away to be seen is effectively
			// an opaque cell; nothing "above" it is going to be visible
			// in the next column, so we might as well treat it as
			// an opaque cell and not scan the cells that are also too
			// far away in the next column.

			boolean currentIsOpaque = !inRadius || isOpaque( x, y, octant );
			if ( wasLastCellOpaque != null )
			{
				if ( currentIsOpaque )
				{
					// We've found a boundary from transparent to opaque. Make a
					// note of it and revisit it later.
					if ( !wasLastCellOpaque.booleanValue() )
					{
						// The new bottom vector touches the upper left corner
						// of
						// opaque cell that is below the transparent cell.
						queue.addFirst( new Column( x + 1, new int[]{ x * 2 - 1, y * 2 + 1 }, topVector, octant ) );
					}
				}
				else if ( wasLastCellOpaque.booleanValue() )
				{
					// We've found a boundary from opaque to transparent. Adjust
					// the top vector so that when we find the next boundary or
					// do
					// the bottom cell, we have the right top vector.
					//
					// The new top vector touches the lower right corner of the
					// opaque cell that is above the transparent cell, which is
					// the upper right corner of the current transparent cell.
					topVector = new int[]{ x * 2 + 1, y * 2 + 1 };
				}
			}
			wasLastCellOpaque = currentIsOpaque;
		}

		// Make a note of the lowest opaque-->transparent transition, if there
		// is one.
		if ( wasLastCellOpaque != null && !wasLastCellOpaque.booleanValue() )
		{
			queue.addFirst( new Column( x + 1, bottomVector, topVector, octant ) );
		}
	}

	// Is the lower-left corner of cell (x,y) within the radius?
	private boolean IsInRadius( int x, int y )
	{
		return Math.abs( x - startX ) <= range && Math.abs( y - startY ) <= range;
	}

	// Octant helpers
	//
	//
	// \2|1/
	// 3\|/0
	// ----+----
	// 4/|\7
	// /5|6\
	//
	//

	private Point TranslateOctant( Point thepos, int octant )
	{
		Point pos = thepos.copy();

		if ( octant == 1 )
		{
			int temp = pos.x;
			pos.x = pos.y;
			pos.y = temp;
		}
		else if ( octant == 2 )
		{
			int temp = pos.x;
			pos.x = pos.y * -1;
			pos.y = temp;
		}
		else if ( octant == 3 )
		{
			pos.x = pos.x * -1;
		}
		else if ( octant == 4 )
		{
			int temp = pos.x * -1;
			pos.x = pos.y * -1;
			pos.y = temp;
		}
		else if ( octant == 5 )
		{
			pos.y = pos.y * -1;
			pos.x = pos.x * -1;
		}
		else if ( octant == 6 )
		{
			int temp = pos.y;
			pos.y = pos.x * -1;
			pos.x = temp;
		}
		else if ( octant == 7 )
		{
			pos.y = pos.y * -1;
		}

		pos.x = ( pos.x ) + startX;
		pos.y = ( pos.y ) + startY;

		return pos;
	}

	private boolean isOpaque( int x, int y, int octant )
	{
		Point temp = Point.obtain();
		Point pos = TranslateOctant( temp.set( x, y ), octant );
		temp.free();

		// hack to prevent start tile from blocking sight
		if ( pos.x == startX && pos.y == startY )
		{
			pos.free();
			return false;
		}

		boolean opaque = false;

		if ( allowOutOfBounds && ( pos.x < 0 || pos.y < 0 || pos.x >= grid.length || pos.y >= grid[ 0 ].length ) )
		{
			opaque = false;
		}
		else
		{
			opaque = !grid[ pos.x ][ pos.y ].getPassable( travelType, self );
		}

		pos.free();

		return opaque;
	}


	private static class Column
	{
		private int X;
		private int[] BottomVector;
		private int[] TopVector;
		private int octant;

		public Column( int x, int[] bottom, int[] top, int octant )
		{
			this.setOctant( octant );
			this.X = x;
			this.BottomVector = bottom;
			this.TopVector = top;
		}

		public int getX()
		{
			return X;
		}

		public void setX( int X )
		{
			this.X = X;
		}

		public int[] getBottomVector()
		{
			return BottomVector;
		}

		public void setBottomVector( int[] v )
		{
			BottomVector = v;
		}

		public int[] getTopVector()
		{
			return TopVector;
		}

		public void setTopVector( int[] v )
		{
			TopVector = v;
		}

		public int getOctant()
		{
			return octant;
		}

		public void setOctant( int octant )
		{
			this.octant = octant;
		}
	}
}
