package com.lyeeedar.Pathfinding;

import com.badlogic.gdx.utils.Array;
import com.lyeeedar.Enums;
import com.lyeeedar.Level.Tile;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;

public final class ShadowCastCache
{
	private final EnumBitflag<Enums.SpaceSlot> LightPassability;

	public ShadowCastCache( EnumBitflag<Enums.SpaceSlot> LightPassability )
	{
		this.LightPassability = LightPassability;
	}

	public ShadowCastCache()
	{
		LightPassability = new EnumBitflag<Enums.SpaceSlot>( Enums.SpaceSlot.WALL );
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

	private int lastrange;
	private int lastx;
	private int lasty;
	private Array<Point> opaqueTiles = new Array<Point>();
	private Array<Point> clearTiles = new Array<Point>();
	private Array<Point> shadowCastOutput = new Array<Point>();

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

			ShadowCaster shadow = new ShadowCaster( grid, range );
			shadow.allowOutOfBounds = allowOutOfBounds;
			shadow.ComputeFOV( x, y, shadowCastOutput );

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
