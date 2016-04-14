package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

public class RandomPlace extends AbstractRoomGenerator
{

	public RandomPlace(  )
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

		for ( int x = 0; x < width; x++ )
		{
			for ( int y = 0; y < height; y++ )
			{
				grid.getArray()[x][y] = ran.nextBoolean() ? floor.copy() : wall.copy();
			}
		}
	}

	@Override
	public void parse( Element xml )
	{
	}

}
