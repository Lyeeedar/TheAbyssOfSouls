package com.lyeeedar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectFloatMap;
import com.badlogic.gdx.utils.XmlReader;
import com.exp4j.Helpers.EquationHelper;
import com.lyeeedar.Util.FastEnumMap;
import com.lyeeedar.Util.Point;

/**
 * Created by Philip on 20-Mar-16.
 */
public class Enums
{
	// ----------------------------------------------------------------------
	public enum Rarity
	{
		COMMON,
		UNCOMMON,
		RARE,
		MYSTICAL,
		LEGENDARY;

		public static final Rarity[] Values = Rarity.values();
	}

	// ----------------------------------------------------------------------
	public enum EquipmentSlot
	{
		WEAPON,
		ARMOUR,
		TRINKET,
		POTION
	}

	// ----------------------------------------------------------------------
	public enum Direction
	{
		CENTRE( 0, 0, "C" ),
		NORTH( 0, 1, "N" ),
		SOUTH( 0, -1, "S" ),
		EAST( 1, 0, "E" ),
		WEST( -1, 0, "W" ),
		NORTHEAST( 1, 1, "NE" ),
		NORTHWEST( -1, 1, "NW" ),
		SOUTHEAST( 1, -1, "SE" ),
		SOUTHWEST( -1, -1, "SW" );

		static
		{
			// Setup neighbours
			Direction.CENTRE.clockwise = Direction.CENTRE;
			Direction.CENTRE.anticlockwise = Direction.CENTRE;

			Direction.NORTH.anticlockwise = Direction.NORTHWEST;
			Direction.NORTH.clockwise = Direction.NORTHEAST;

			Direction.NORTHEAST.anticlockwise = Direction.NORTH;
			Direction.NORTHEAST.clockwise = Direction.EAST;

			Direction.EAST.anticlockwise = Direction.NORTHEAST;
			Direction.EAST.clockwise = Direction.SOUTHEAST;

			Direction.SOUTHEAST.anticlockwise = Direction.EAST;
			Direction.SOUTHEAST.clockwise = Direction.SOUTH;

			Direction.SOUTH.anticlockwise = Direction.SOUTHEAST;
			Direction.SOUTH.clockwise = Direction.SOUTHWEST;

			Direction.SOUTHWEST.anticlockwise = Direction.SOUTH;
			Direction.SOUTHWEST.clockwise = Direction.WEST;

			Direction.WEST.anticlockwise = Direction.SOUTHWEST;
			Direction.WEST.clockwise = Direction.NORTHWEST;

			Direction.NORTHWEST.anticlockwise = Direction.WEST;
			Direction.NORTHWEST.clockwise = Direction.NORTH;

			// Setup is cardinal
			Direction.NORTH.isCardinal = true;
			Direction.SOUTH.isCardinal = true;
			Direction.EAST.isCardinal = true;
			Direction.WEST.isCardinal = true;
		}

		public final String identifier;
		public final int x;
		public final int y;
		private final float angle; // In degrees
		private Direction clockwise;
		private Direction anticlockwise;
		private boolean isCardinal = false;
		public static final Direction[] CardinalValues = {NORTH, EAST, SOUTH, WEST};
		public static final Direction[] DiagonalValues = {NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHWEST};
		public static final Direction[] Values = Direction.values();

		Direction( int x, int y, String identifier )
		{
			this.x = x;
			this.y = y;
			this.identifier = identifier;

			// basis vector = 0, 1
			double dot = 0 * x + 1 * y; // dot product
			double det = 0 * y - 1 * x; // determinant
			angle = (float) Math.atan2( det, dot ) * MathUtils.radiansToDegrees;
		}

		public static Direction getDirection( Point point )
		{
			return getDirection( point.x, point.y );
		}

		public static Direction getDirection( float[] dir )
		{
			int x = dir[0] < 0 ? -1 : dir[0] > 0 ? 1 : 0;
			int y = dir[1] < 0 ? -1 : dir[1] > 0 ? 1 : 0;

			return getDirection( x, y );
		}

		public static Direction getDirection( int[] dir )
		{
			return getDirection( dir[ 0 ], dir[ 1 ] );
		}

		public static Direction getDirection( int dx, int dy )
		{
			dx = MathUtils.clamp( dx, -1, 1 );
			dy = MathUtils.clamp( dy, -1, 1 );

			Direction d = Direction.CENTRE;

			for ( Direction dir : Direction.Values )
			{
				if ( dir.x == dx && dir.y == dy )
				{
					d = dir;
					break;
				}
			}

			return d;
		}

		public static Direction getCardinalDirection( int dx, int dy )
		{
			if ( dx == 0 && dy == 0 )
			{
				return Direction.CENTRE;
			}

			if ( Math.abs( dx ) > Math.abs( dy ) )
			{
				if ( dx < 0 )
				{
					return Direction.WEST;
				}
				else
				{
					return Direction.EAST;
				}
			}
			else
			{
				if ( dy < 0 )
				{
					return Direction.SOUTH;
				}
				else
				{
					return Direction.NORTH;
				}
			}
		}

		public static Direction getDirection( Point p1, Point p2 )
		{
			return getDirection( p2.x - p1.x, p2.y - p1.y );
		}

		public static Array<Point> buildCone( Direction dir, Point start, int range )
		{
			Array<Point> hitTiles = new Array<Point>();

			Direction anticlockwise = dir.getAnticlockwise();
			Direction clockwise = dir.getClockwise();

			Point acwOffset = Point.obtain().set( dir.x - anticlockwise.x, dir.y - anticlockwise.y );
			Point cwOffset = Point.obtain().set( dir.x - clockwise.x, dir.y - clockwise.y );

			hitTiles.add( Point.obtain().set( start.x + anticlockwise.x, start.y + anticlockwise.y ) );

			hitTiles.add( Point.obtain().set( start.x + dir.x, start.y + dir.y ) );

			hitTiles.add( Point.obtain().set( start.x + clockwise.x, start.y + clockwise.y ) );

			for ( int i = 2; i <= range; i++ )
			{
				int acx = start.x + anticlockwise.x * i;
				int acy = start.y + anticlockwise.y * i;

				int nx = start.x + dir.x * i;
				int ny = start.y + dir.y * i;

				int cx = start.x + clockwise.x * i;
				int cy = start.y + clockwise.y * i;

				// add base tiles
				hitTiles.add( Point.obtain().set( acx, acy ) );
				hitTiles.add( Point.obtain().set( nx, ny ) );
				hitTiles.add( Point.obtain().set( cx, cy ) );

				// add anticlockwise - mid
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = acx + acwOffset.x * ii;
					int py = acy + acwOffset.y * ii;

					hitTiles.add( Point.obtain().set( px, py ) );
				}

				// add mid - clockwise
				for ( int ii = 1; ii <= range; ii++ )
				{
					int px = cx + cwOffset.x * ii;
					int py = cy + cwOffset.y * ii;

					hitTiles.add( Point.obtain().set( px, py ) );
				}
			}

			acwOffset.free();
			cwOffset.free();

			return hitTiles;
		}

		public Direction getClockwise()
		{
			return clockwise;
		}

		public Direction getAnticlockwise()
		{
			return anticlockwise;
		}

		public boolean isCardinal()
		{
			return isCardinal;
		}

		public float getAngle()
		{
			return angle;
		}

		public Direction getOpposite()
		{
			return getDirection( x * -1, y * -1 );
		}
	}

	// ----------------------------------------------------------------------
	public enum SpaceSlot
	{
		FLOOR,
		WALL,
		ENTITY,
		AIR;

		public static final SpaceSlot[] Values = SpaceSlot.values();
		public static final SpaceSlot[] BasicValues = { FLOOR, WALL };
		public static final SpaceSlot[] InterestingValues = {ENTITY, AIR};
	}

	// ----------------------------------------------------------------------
	public enum Statistic
	{
		MAX_HEALTH,
		MAX_STAMINA,
		SIGHT,
		MORALE,

		PHYSICAL_ATTACK,
		SHOCK_ATTACK,
		ACID_ATTACK,
		ICE_ATTACK,
		VORPAL_ATTACK,

		PHYSICAL_DEFENSE,
		SHOCK_DEFENSE,
		ACID_DEFENSE,
		ICE_DEFENSE,
		VORPAL_DEFENSE;

		public static final Statistic[] ATTACK_STATS = { PHYSICAL_ATTACK, SHOCK_ATTACK, ACID_ATTACK, ICE_ATTACK, VORPAL_ATTACK };
		public static final Statistic[] DEFENSE_STATS = { PHYSICAL_DEFENSE, SHOCK_DEFENSE, ACID_DEFENSE, ICE_DEFENSE, VORPAL_DEFENSE };
		public static final Statistic[] Values = Statistic.values();

		static
		{

		}

		public static ObjectFloatMap<String> getEmptyMap()
		{
			ObjectFloatMap<String> emptyMap = new ObjectFloatMap<String>();

			for ( Statistic s : Statistic.Values )
			{
				emptyMap.put( s.toString().toLowerCase(), 0f );
			}

			return emptyMap;
		}

		public static ObjectFloatMap<String> statsBlockToVariableBlock( FastEnumMap<Statistic, Float> stats )
		{
			ObjectFloatMap<String> variableMap = new ObjectFloatMap<String>();

			for ( Statistic key : Statistic.Values )
			{
				Float val = stats.get( key );
				if ( val != null )
				{
					variableMap.put( key.toString().toLowerCase(), val );
				}
			}

			return variableMap;
		}

		public static FastEnumMap<Statistic, Float> getStatisticsBlock()
		{
			return getStatisticsBlock(0f);
		}

		public static FastEnumMap<Statistic, Float> getStatisticsBlock(float defaultValue)
		{
			FastEnumMap<Statistic, Float> stats = new FastEnumMap<Statistic, Float>( Statistic.class );

			for ( Statistic stat : Statistic.Values )
			{
				stats.put( stat, defaultValue );
			}

			return stats;
		}

		public static FastEnumMap<Statistic, Float> load( XmlReader.Element xml )
		{
			FastEnumMap<Statistic, Float> map = new FastEnumMap<Statistic, Float>( Statistic.class );
			for (Statistic stat : Statistic.Values)
			{
				map.put( stat, 0f );
			}

			return load(xml, map);
		}

		public static FastEnumMap<Statistic, Float> load( XmlReader.Element xml, FastEnumMap<Statistic, Float> values )
		{
			if (xml != null)
			{
				for ( int i = 0; i < xml.getChildCount(); i++ )
				{

					XmlReader.Element el = xml.getChild( i );

					Statistic stat = Statistic.valueOf( el.getName().toUpperCase() );
					String eqn = el.getText().toLowerCase();

					float newVal = values.get( stat );

					ObjectFloatMap<String> variableMap = new ObjectFloatMap<String>();
					variableMap.put( "value", newVal );
					variableMap.put( "val", newVal );

					newVal = EquationHelper.evaluate( eqn, variableMap );

					values.put( stat, newVal );
				}
			}

			return values;
		}

		public static FastEnumMap<Statistic, Float> copy( FastEnumMap<Statistic, Float> map )
		{
			FastEnumMap<Statistic, Float> newMap = new FastEnumMap<Statistic, Float>( Statistic.class );
			for ( Statistic e : Statistic.Values )
			{
				newMap.put( e, map.get( e ) );
			}
			return newMap;
		}

		public static String formatString( String input )
		{
			String[] words = input.split( "_" );

			String output = "";

			for ( String word : words )
			{
				word = word.toLowerCase();
				output += word + " ";
			}

			return output.trim();
		}
	}
}
