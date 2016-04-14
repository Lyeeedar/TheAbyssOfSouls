package com.lyeeedar.DungeonGeneration.RoomGenerators;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Created by Philip on 11-Feb-16.
 */
public class Island extends AbstractRoomGenerator
{
	private Array<Feature> features = new Array<Feature>();

	public Island( )
	{
		super( false );
	}

	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		int width = grid.getXSize();
		int height = grid.getYSize();

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );

		MidpointDisplacement midpointDisplacement = new MidpointDisplacement( ran, width, height );

		float[][] map = midpointDisplacement.getMap();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				float val = map[x][y];
				Symbol symbol = null;
				for ( Feature feature : features )
				{
					if (feature.minVal <= val && feature.maxVal >= val)
					{
						symbol = feature.getAsSymbol( );
						break;
					}
				}

				if (symbol != null)
				{
					symbol.resolve( symbolMap );
					grid.getArray()[x][y] = symbol.copy();
				}
			}
		}
	}

	@Override
	public void parse( XmlReader.Element xml )
	{
		for ( int i = 0; i < xml.getChildCount(); i++ )
		{
			XmlReader.Element featureElement = xml.getChild( i );
			Feature feature = Feature.load( featureElement );
			features.add( feature );
		}
	}

	// ----------------------------------------------------------------------
	public static class Feature
	{
		public Symbol symbol;
		public float minVal;
		public float maxVal;

		public static Feature load( XmlReader.Element xml )
		{
			Feature feature = new Feature();

			feature.symbol = Symbol.load( xml );

			feature.minVal = xml.getFloat( "MinVal", 0 );
			feature.maxVal = xml.getFloat( "MaxVal", 1 );

			return feature;
		}

		public Symbol getAsSymbol( )
		{
			return symbol;
		}
	}
}
