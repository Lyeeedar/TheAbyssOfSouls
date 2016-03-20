package com.lyeeedar.Util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.security.Key;
import java.util.HashMap;

/**
 * Created by Philip on 27-Feb-16.
 */
public class Controls
{
	public enum Keys
	{
		LEFT,
		RIGHT,
		UP,
		DOWN,
		CANCEL,
		ACCEPT,
		WAIT,
		UPGRADE,
		TOGGLE_AUTOATTACK,
		WEAPON_RANGE,
		ABILITY_1,
		ABILITY_2,
		ABILITY_3,
		ABILITY_4,
		ABILITY_5
	}

	public static Keys[] AbilityKeys = {Keys.ABILITY_1, Keys.ABILITY_2, Keys.ABILITY_3, Keys.ABILITY_4, Keys.ABILITY_5};

	private FastEnumMap<Keys, Integer> keyMap = new FastEnumMap<Keys, Integer>( Keys.class );

	public Controls()
	{
		defaultArrow();
	}

	public void defaultArrow()
	{
		keyMap.put( Keys.LEFT, Input.Keys.LEFT );
		keyMap.put( Keys.RIGHT, Input.Keys.RIGHT );
		keyMap.put( Keys.UP, Input.Keys.UP );
		keyMap.put( Keys.DOWN, Input.Keys.DOWN );
		keyMap.put( Keys.CANCEL, Input.Keys.ESCAPE );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.SPACE );
		keyMap.put( Keys.UPGRADE, Input.Keys.U );
		keyMap.put( Keys.TOGGLE_AUTOATTACK, Input.Keys.F );
		keyMap.put( Keys.WEAPON_RANGE, Input.Keys.R );
		keyMap.put( Keys.ABILITY_1, Input.Keys.NUM_1 );
		keyMap.put( Keys.ABILITY_2, Input.Keys.NUM_2 );
		keyMap.put( Keys.ABILITY_3, Input.Keys.NUM_3 );
		keyMap.put( Keys.ABILITY_4, Input.Keys.NUM_4 );
		keyMap.put( Keys.ABILITY_5, Input.Keys.NUM_5 );
	}

	public void defaultWASD()
	{
		keyMap.put( Keys.LEFT, Input.Keys.A );
		keyMap.put( Keys.RIGHT, Input.Keys.D );
		keyMap.put( Keys.UP, Input.Keys.W );
		keyMap.put( Keys.DOWN, Input.Keys.S );
		keyMap.put( Keys.CANCEL, Input.Keys.ESCAPE );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.SPACE );
		keyMap.put( Keys.UPGRADE, Input.Keys.U );
		keyMap.put( Keys.TOGGLE_AUTOATTACK, Input.Keys.F );
		keyMap.put( Keys.WEAPON_RANGE, Input.Keys.R );
		keyMap.put( Keys.ABILITY_1, Input.Keys.NUM_1 );
		keyMap.put( Keys.ABILITY_2, Input.Keys.NUM_2 );
		keyMap.put( Keys.ABILITY_3, Input.Keys.NUM_3 );
		keyMap.put( Keys.ABILITY_4, Input.Keys.NUM_4 );
		keyMap.put( Keys.ABILITY_5, Input.Keys.NUM_5 );
	}

	public void defaultNumPad()
	{
		keyMap.put( Keys.LEFT, Input.Keys.NUMPAD_4 );
		keyMap.put( Keys.RIGHT, Input.Keys.NUMPAD_6 );
		keyMap.put( Keys.UP, Input.Keys.NUMPAD_8 );
		keyMap.put( Keys.DOWN, Input.Keys.NUMPAD_2 );
		keyMap.put( Keys.CANCEL, Input.Keys.PERIOD );
		keyMap.put( Keys.ACCEPT, Input.Keys.ENTER );
		keyMap.put( Keys.WAIT, Input.Keys.NUMPAD_5 );
		keyMap.put( Keys.UPGRADE, Input.Keys.PLUS );
		keyMap.put( Keys.TOGGLE_AUTOATTACK, Input.Keys.MINUS );
		keyMap.put( Keys.WEAPON_RANGE, Input.Keys.STAR );
		keyMap.put( Keys.ABILITY_1, Input.Keys.NUM_1 );
		keyMap.put( Keys.ABILITY_2, Input.Keys.NUM_2 );
		keyMap.put( Keys.ABILITY_3, Input.Keys.NUM_3 );
		keyMap.put( Keys.ABILITY_4, Input.Keys.NUM_4 );
		keyMap.put( Keys.ABILITY_5, Input.Keys.NUM_5 );
	}

	public void setKeyMap(Keys key, int keycode)
	{
		keyMap.put( key, keycode );
	}

	public int getKeyCode( Keys key )
	{
		return keyMap.get( key );
	}

	public int toAbilityKey( int keycode )
	{
		for (int i = 0; i < AbilityKeys.length; i++)
		{
			if (isKey( AbilityKeys[i], keycode ))
			{
				return i;
			}
		}

		return -1;
	}

	public boolean isKey(Keys key, int keycode)
	{
		return keyMap.get( key ) == keycode;
	}

	public boolean isKeyDown(Keys key)
	{
		return Gdx.input.isKeyPressed( keyMap.get( key ) );
	}
}
