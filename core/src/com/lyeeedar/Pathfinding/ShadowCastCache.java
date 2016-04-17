package com.lyeeedar.Pathfinding;

import com.badlogic.gdx.utils.Array;
import com.lyeeedar.Enums;
import com.lyeeedar.Level.Tile;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;

public final class ShadowCastCache
{
	public ShadowCastCache( Enums.SpaceSlot LightPassability )
	{
		this(LightPassability, FOV.SHADOW);
	}

	public ShadowCastCache()
	{
		this( Enums.SpaceSlot.WALL );
	}

	public ShadowCastCache(int fovType)
	{
		this( Enums.SpaceSlot.WALL, fovType );
	}

	public ShadowCastCache(Enums.SpaceSlot LightPassability, int fovType)
	{
		this.LightPassability = LightPassability;
		fov = new FOV( fovType );
	}

	public ShadowCastCache copy()
	{
		ShadowCastCache cache = new ShadowCastCache( LightPassability );
		cache.lastrange = lastrange;
		cache.lastx = lastx;
		cache.lasty = lasty;

		for ( Point p : opaqueTiles )
		{
			cache.opaqueTiles.add( p.copy() );
		}

		for ( Point p : shadowCastOutput )
		{
			cache.shadowCastOutput.add( p.copy() );
		}

		return cache;
	}

	private final FOV fov;
	private final Enums.SpaceSlot LightPassability;

	private int lastrange;
	private int lastx;
	private int lasty;
	private Array<Point> opaqueTiles = new Array<Point>();
	private Array<Point> clearTiles = new Array<Point>();
	private Array<Point> shadowCastOutput = new Array<Point>();
	private double[][] rawOutput;

	public int getLastx() { return lastx; }
	public int getLasty() { return lasty; }
	public int getLastrange() { return lastrange; }

	public double[][] getRawOutput() { return rawOutput; }

	public Array<Point> getCurrentShadowCast()
	{
		return shadowCastOutput;
	}

	public Array<Point> getShadowCast( Tile[][] grid, int x, int y, int range, Object caster )
	{
		return getShadowCast( grid, x, y, range, caster, false );
	}

	public Array<Point> getShadowCast( Tile[][] grid, int x, int y, int range, Object caster, boolean allowOutOfBounds )
	{
		boolean recalculate = false;

		if ( x != lastx || y != lasty )
		{
			recalculate = true;
		}
		else if ( range != lastrange )
		{
			recalculate = true;
		}
		else
		{
			for ( Point pos : opaqueTiles )
			{
				Tile tile = grid[pos.x][pos.y];
				if ( tile.getPassable( LightPassability, caster ) )
				{
					recalculate = true; // something has moved
					break;
				}
			}

			if ( !recalculate )
			{
				for ( Point pos : clearTiles )
				{
					Tile tile = grid[pos.x][pos.y];
					if ( !tile.getPassable( LightPassability, caster ) )
					{
						recalculate = true; // something has moved
						break;
					}
				}
			}
		}

		if ( recalculate )
		{
			Point.pool.freeAll( shadowCastOutput );
			shadowCastOutput.clear();

			// build grid
			double[][] resistanceGrid = new double[range*2][range*2];
			for (int ix = 0; ix < range*2; ix++)
			{
				for (int iy = 0; iy < range*2; iy++)
				{
					int gx = ix + x - range;
					int gy = iy + y - range;

					if (gx >= 0 && gx < grid.length && gy >= 0 && gy < grid[0].length)
					{
						resistanceGrid[ix][iy] = grid[gx][gy].getPassable( LightPassability, caster ) ? 0 : 1;
					}
					else
					{
						resistanceGrid[ix][iy] = 1;
					}
				}
			}

			rawOutput = fov.calculateFOV( resistanceGrid, range, range, range, Radius.SQUARE );

			for (int ix = 0; ix < range*2; ix++)
			{
				for ( int iy = 0; iy < range * 2; iy++ )
				{
					int gx = ix + x - range;
					int gy = iy + y - range;

					if (rawOutput[ix][iy] > 0 && gx >= 0 && gx < grid.length && gy >= 0 && gy < grid[0].length)
					{
						shadowCastOutput.add( Point.obtain().set( gx, gy ) );
					}
				}
			}

			// build list of clear/opaque
			opaqueTiles.clear();
			clearTiles.clear();

			for ( Point pos : shadowCastOutput )
			{
				if ( pos.x < 0 || pos.y < 0 || pos.x >= grid.length || pos.y >= grid[0].length )
				{
					continue;
				}

				Tile tile = grid[pos.x][pos.y];
				if ( !tile.getPassable( LightPassability, caster ) )
				{
					opaqueTiles.add( pos );
				}
				else
				{
					clearTiles.add( pos );
				}
			}
			lastx = x;
			lasty = y;
			lastrange = range;
		}

		return shadowCastOutput;
	}
}
