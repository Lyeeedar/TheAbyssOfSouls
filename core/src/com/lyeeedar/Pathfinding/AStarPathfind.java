/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.lyeeedar.Pathfinding;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BinaryHeap;
import com.lyeeedar.Enums;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;

public class AStarPathfind
{
	private static final int[][] NormalOffsets = { { -1, 0 }, { 0, -1 }, { +1, 0 }, { 0, +1 } };

	private static final int[][] DiagonalOffsets = { { -1, -1 }, { -1, +1 }, { +1, -1 }, { +1, +1 } };

	private final PathfindingTile[][] grid;
	private final int width;
	private final int height;
	private final boolean canMoveDiagonal;
	private final int actorSize;
	private final boolean findOptimal;
	private final EnumBitflag<Enums.SpaceSlot> travelType;
	private final Object self;

	private final int startx;
	private final int starty;
	private final int endx;
	private final int endy;
	private int currentx;
	private int currenty;
	private Node[][] nodes;

	public boolean debug = false;

	private BinaryHeap<Node> openList = new BinaryHeap<Node>();

	public AStarPathfind( PathfindingTile[][] grid, int startx, int starty, int endx, int endy, boolean canMoveDiagonal, boolean findOptimal, int actorSize, EnumBitflag<Enums.SpaceSlot> travelType, Object self )
	{
		this.grid = grid;
		this.width = grid.length;
		this.height = grid[0].length;
		this.canMoveDiagonal = canMoveDiagonal;
		this.actorSize = actorSize;
		this.findOptimal = findOptimal;
		this.travelType = travelType;
		this.self = self;

		this.startx = MathUtils.clamp( startx, 0, width - 1 );
		this.starty = MathUtils.clamp( starty, 0, height - 1 );

		this.endx = MathUtils.clamp( endx, 0, width - 1 );
		this.endy = MathUtils.clamp( endy, 0, height - 1 );

		this.currentx = this.startx;
		this.currenty = this.starty;
	}

	private void path()
	{
		Node current = openList.pop();

		currentx = current.x;
		currenty = current.y;

		if ( isEnd( currentx, currenty ) ) { return; }

		for ( int[] offset : NormalOffsets )
		{
			addNodeToOpenList( current.x + offset[0], current.y + offset[1], current );
		}

		if ( canMoveDiagonal )
		{
			for ( int[] offset : DiagonalOffsets )
			{
				addNodeToOpenList( current.x + offset[0], current.y + offset[1], current );
			}
		}

		current.processed = true;
	}

	private boolean isStart( int x, int y )
	{
		return x == startx && y == starty;
	}

	private boolean isEnd( int x, int y )
	{
		return x == endx && y == endy;
	}

	private void addNodeToOpenList( int x, int y, Node parent )
	{
		if ( !isStart( x, y ) && !isEnd( x, y ) )
		{
			for ( int ix = 0; ix < actorSize; ix++ )
			{
				for ( int iy = 0; iy < actorSize; iy++ )
				{
					if ( isColliding( x + ix, y + iy ) ) { return; }
				}
			}
		}

		int heuristic = Math.abs( x - endx ) + Math.abs( y - endy );
		int cost = heuristic + ( parent != null ? parent.cost : 0 );

		cost += grid[x][y].getInfluence( travelType, self );

		// 3 possible conditions

		Node node = nodes[x][y];

		// not added to open list yet, so add it
		if ( node == null )
		{
			node = new Node( x, y );
			node.cost = cost;
			node.parent = parent;
			openList.add( node, node.cost );

			nodes[x][y] = node;
		}

		// not yet processed, if lower cost update the values and reposition in
		// list
		else if ( !node.processed )
		{
			if ( cost < node.cost )
			{
				node.cost = cost;
				node.parent = parent;

				openList.setValue( node, node.cost );
			}
		}

		// processed, if lower cost then update parent and cost
		else
		{
			if ( cost < node.cost )
			{
				node.cost = cost;
				node.parent = parent;
			}
		}
	}

	public boolean isColliding( int x, int y )
	{
		if ( x < 0 || y < 0 || x >= width || y >= height || grid[x][y] == null || !grid[x][y].getPassable( travelType, self ) ) { return true; }
		return false;
	}

	public Array<Point> getPath()
	{
		nodes = new Node[width][height];

		addNodeToOpenList( startx, starty, null );

		while ( ( findOptimal || !isEnd( currentx, currenty ) ) && openList.size > 0 )
		{
			path();
		}

		if ( nodes[endx][endy] == null )
		{
			return null;
		}
		else
		{
			Array<Point> path = new Array<Point>();

			path.add( Point.obtain().set( endx, endy ) );

			Node node = nodes[endx][endy];

			while ( node != null )
			{
				path.add( Point.obtain().set( node.x, node.y ) );

				node = node.parent;
			}

			path.reverse();

			return path;
		}
	}

	private class Node extends BinaryHeap.Node
	{
		public int x;
		public int y;
		public int cost;
		public Node parent;

		public boolean processed = false;

		public Node( int x, int y )
		{
			super(0);

			this.x = x;
			this.y = y;
		}

		@Override
		public String toString()
		{
			return "" + cost;
		}
	}

}
