package com.lyeeedar.Sound;

import java.io.IOException;
import java.util.HashSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.lyeeedar.AssetManager;
import com.lyeeedar.Components.Mappers;
import com.lyeeedar.Components.PositionComponent;
import com.lyeeedar.Enums;
import com.lyeeedar.GlobalData;
import com.lyeeedar.Level.Tile;
import com.lyeeedar.Pathfinding.AStarPathfind;
import com.lyeeedar.Util.EnumBitflag;
import com.lyeeedar.Util.Point;

public class SoundInstance
{
	private final EnumBitflag<Enums.SpaceSlot> SoundPassability = new EnumBitflag<Enums.SpaceSlot>( Enums.SpaceSlot.WALL );

	public Sound sound;
	public String name;

	public float minPitch = 0.7f;
	public float maxPitch = 1.5f;
	public float volume = 0.5f;

	public int range = 10;
	public int falloffMin = 5;

	public HashSet<String> shoutFaction;
	public String key;
	public Object value;

	public SoundInstance()
	{

	}

	public SoundInstance( Sound sound )
	{
		this.sound = sound;
	}

	public static SoundInstance load( Element xml )
	{
		SoundInstance sound = new SoundInstance();
		sound.name = xml.get( "Name" );
		sound.sound = AssetManager.loadSound( sound.name );
		sound.range = xml.getInt( "Range", sound.range );
		sound.falloffMin = xml.getInt( "FalloffMin", sound.falloffMin );
		sound.volume = xml.getFloat( "Volume", sound.volume );

		sound.minPitch = xml.getFloat( "Pitch", sound.minPitch );
		sound.minPitch = xml.getFloat( "Pitch", sound.maxPitch );

		sound.minPitch = xml.getFloat( "MinPitch", sound.minPitch );
		sound.maxPitch = xml.getFloat( "MaxPitch", sound.maxPitch );

		return sound;
	}

	public SoundInstance copy()
	{
		SoundInstance soundInstance = new SoundInstance(  );
		soundInstance.sound = sound;
		soundInstance.name = name;
		soundInstance.minPitch = minPitch;
		soundInstance.maxPitch = maxPitch;
		soundInstance.volume = volume;
		soundInstance.range = range;
		soundInstance.falloffMin = falloffMin;
		soundInstance.shoutFaction = shoutFaction;
		soundInstance.key = key;
		soundInstance.value = value;

		return soundInstance;
	}

	public void play( Tile tile )
	{
		// calculate data propogation
		float playerDist = Integer.MAX_VALUE;
		PositionComponent playerPos = Mappers.position.get( tile.level.player );
		Point shoutSource = Point.obtain().set( tile.x, tile.y );

		int maxAudibleDist = range;// ( range / 4 ) * 3;

		if ( key != null )
		{

		}
		else
		{
			playerDist = Vector2.dst( tile.x, tile.y, playerPos.getPosition().x, playerPos.getPosition().y );
		}

		if (GlobalData.Global.effectVolume > 0)
		{
			// calculate sound play volume
			if ( playerDist <= range && sound != null )
			{
				float vol = volume * GlobalData.Global.effectVolume;

				if ( playerDist > falloffMin )
				{
					float alpha = 1 - ( playerDist - falloffMin ) / ( range - falloffMin );
					vol *= alpha;
				}

				float xdiff = tile.x - playerPos.getPosition().x;
				xdiff /= range;

				sound.play( vol, minPitch + MathUtils.random() * ( maxPitch - minPitch ), xdiff );
			}
		}
	}

	private static final ObjectMap<String, Element> soundMap = new ObjectMap<String, Element>(  );
	private static boolean loaded = false;
	public static SoundInstance getSound( String name )
	{
		if ( !loaded )
		{
			loaded = true;

			XmlReader reader = new XmlReader();
			Element xml = null;

			try
			{
				xml = reader.parse( Gdx.files.internal( "Sound/SoundMap.xml" ) );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

			for ( int i = 0; i < xml.getChildCount(); i++ )
			{
				Element el = xml.getChild( i );
				soundMap.put( el.getName(), el );
			}
		}

		if ( soundMap.containsKey( name ) )
		{
			return SoundInstance.load( soundMap.get( name ) );
		}
		else
		{
			SoundInstance sound = new SoundInstance(  );
			sound.name = name;
			sound.sound = AssetManager.loadSound( name );
			return sound;
		}
	}
}
