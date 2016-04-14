package com.lyeeedar.DungeonGeneration.RoomGenerators;

import java.util.Random;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.DungeonGeneration.Data.Symbol;
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator;
import com.lyeeedar.Util.Array2D;
import org.jetbrains.annotations.NotNull;

public class OverlappingRects extends AbstractRoomGenerator
{
	private static final float V_WIDTH_MIN = 0.3f;
	private static final float V_WIDTH_MAX = 0.9f;
	private static final float V_WIDTH_DIFF = V_WIDTH_MAX - V_WIDTH_MIN;
	private static final float V_HEIGHT_MIN = 0.7f;
	private static final float V_HEIGHT_MAX = 1.0f;
	private static final float V_HEIGHT_DIFF = V_HEIGHT_MAX - V_HEIGHT_MIN;

	private static final float H_WIDTH_MIN = 0.9f;
	private static final float H_WIDTH_MAX = 1.0f;
	private static final float H_WIDTH_DIFF = H_WIDTH_MAX - H_WIDTH_MIN;
	private static final float H_HEIGHT_MIN = 0.3f;
	private static final float H_HEIGHT_MAX = 0.9f;
	private static final float H_HEIGHT_DIFF = H_HEIGHT_MAX - H_HEIGHT_MIN;

	public OverlappingRects()
	{
		super( true );
	}

	@Override
	public void process( @NotNull Array2D<Symbol> grid, @NotNull ObjectMap<Character, Symbol> symbolMap, @NotNull Random ran )
	{
		int width = grid.getXSize() - 2;
		int height = grid.getYSize() - 2;

		Symbol wall = symbolMap.get( '#' );
		Symbol floor = symbolMap.get( '.' );

		// calculate vertical rect
		float vwidthP = ran.nextFloat() * V_WIDTH_DIFF + V_WIDTH_MIN;
		float vheightP = ran.nextFloat() * V_HEIGHT_DIFF + V_HEIGHT_MIN;

		int vwidth = (int) ( width * vwidthP );
		int vwidthdiff = width - vwidth;
		int vxoffset = vwidthdiff > 1 ? ran.nextInt( vwidthdiff / 2 ) : 0;

		int vheight = (int) ( height * vheightP );
		int vheightdiff = height - vheight;
		int vyoffset = vheightdiff > 1 ? ran.nextInt( vheightdiff / 2 ) : 0;

		// calculate horizontal rect
		float hwidthP = ran.nextFloat() * H_WIDTH_DIFF + H_WIDTH_MIN;
		float hheightP = ran.nextFloat() * H_HEIGHT_DIFF + H_HEIGHT_MIN;

		int hwidth = (int) ( width * hwidthP );
		int hwidthdiff = width - hwidth;
		int hxoffset = hwidthdiff > 1 ? ran.nextInt( hwidthdiff / 2 ) : 0;

		int hheight = (int) ( height * hheightP );
		int hheightdiff = height - hheight;
		int hyoffset = hheightdiff > 1 ? ran.nextInt( hheightdiff / 2 ) : 0;

		// intialise to solid
		for ( int x = 0; x < width + 2; x++ )
		{
			for ( int y = 0; y < height + 2; y++ )
			{
				grid.getArray()[x][y] = wall.copy();
			}
		}

		// place vertical rect
		for ( int x = vxoffset; x < vxoffset + vwidth; x++ )
		{
			for ( int y = vyoffset; y < vyoffset + vheight; y++ )
			{
				grid.getArray()[x + 1][y + 1] = floor.copy();
			}
		}

		// place horizontal rect
		for ( int x = hxoffset; x < hxoffset + hwidth; x++ )
		{
			for ( int y = hyoffset; y < hyoffset + hheight; y++ )
			{
				grid.getArray()[x + 1][y + 1] = floor.copy();
			}
		}
	}

	@Override
	public void parse( Element xml )
	{
	}
}
