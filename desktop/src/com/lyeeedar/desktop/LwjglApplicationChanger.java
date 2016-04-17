package com.lyeeedar.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import com.lyeeedar.AbstractApplicationChanger;
import com.lyeeedar.GlobalData;
import com.lyeeedar.MainGame;
import com.lyeeedar.Util.Controls;

public class LwjglApplicationChanger extends AbstractApplicationChanger
{
	public Preferences prefs;

	public LwjglApplicationChanger()
	{
		super( new LwjglPreferences( "game-settings", "settings" ) );
	}

	@Override
	public Application createApplication( MainGame game, Preferences pref )
	{
		System.setProperty( "org.lwjgl.opengl.Window.undecorated", "" + pref.getBoolean( "borderless" ) );

		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();

		cfg.title = "A Skin of Others";
		cfg.width = pref.getInteger( "resolutionX" );
		cfg.height = pref.getInteger( "resolutionY" );
		cfg.fullscreen = pref.getBoolean( "fullscreen" );
		cfg.vSyncEnabled = pref.getBoolean( "vSync" );
		cfg.foregroundFPS = 0;
		cfg.backgroundFPS = 2;
		cfg.samples = pref.getInteger( "msaa" );
		cfg.addIcon( "Sprites/Unpacked/Icon32.png", FileType.Internal );
		cfg.allowSoftwareMode = true;

		GlobalData.Global.targetResolution[0] = cfg.width;
		GlobalData.Global.targetResolution[1] = cfg.height;
		GlobalData.Global.screenSize[0] = cfg.width;
		GlobalData.Global.screenSize[1] = cfg.height;

		GlobalData.Global.fps = pref.getInteger( "fps" );
		GlobalData.Global.animationSpeed = 1.0f / pref.getFloat( "animspeed" );

		GlobalData.Global.movementTypePathfind = pref.getBoolean( "pathfindMovement" );
		GlobalData.Global.musicVolume = pref.getFloat( "musicVolume" );
		GlobalData.Global.ambientVolume = pref.getFloat( "ambientVolume" );
		GlobalData.Global.effectVolume = pref.getFloat( "effectVolume" );

		for ( Controls.Keys key : Controls.Keys.values() )
		{
			GlobalData.Global.controls.setKeyMap( key, pref.getInteger( key.toString() ) );
		}

		return new LwjglApplication( game, cfg );
	}

	@Override
	public void processResources()
	{
		if (!GlobalData.Global.release)
		{
			new AtlasCreator();
			new QuestProcessor();
		}
	}

	@Override
	public void updateApplication( Preferences pref )
	{
		System.setProperty( "org.lwjgl.opengl.Window.undecorated", "" + pref.getBoolean( "borderless" ) );

		int width = pref.getInteger( "resolutionX" );
		int height = pref.getInteger( "resolutionY" );
		boolean fullscreen = pref.getBoolean( "fullscreen" );

		GlobalData.Global.targetResolution[0] = width;
		GlobalData.Global.targetResolution[1] = height;
		GlobalData.Global.fps = pref.getInteger( "fps" );
		GlobalData.Global.animationSpeed = 1.0f / pref.getFloat( "animspeed" );

		GlobalData.Global.movementTypePathfind = pref.getBoolean( "pathfindMovement" );
		GlobalData.Global.musicVolume = pref.getFloat( "musicVolume" );
		GlobalData.Global.ambientVolume = pref.getFloat( "ambientVolume" );
		GlobalData.Global.effectVolume = pref.getFloat( "effectVolume" );

		for ( Controls.Keys key : Controls.Keys.values() )
		{
			GlobalData.Global.controls.setKeyMap( key, pref.getInteger( key.toString() ) );
		}

		if (fullscreen)
		{
			DisplayMode mode = Gdx.graphics.getDisplayMode();
			Gdx.graphics.setFullscreenMode( mode );
		}
		else
		{
			Gdx.graphics.setWindowedMode( width, height );
		}
	}

	@Override
	public String[] getSupportedDisplayModes()
	{
		DisplayMode[] displayModes = Gdx.graphics.getDisplayModes();

		ArrayList<String> modes = new ArrayList<String>();

		for ( int i = 0; i < displayModes.length; i++ )
		{
			String mode = displayModes[i].width + "x" + displayModes[i].height;

			boolean contained = false;
			for ( String m : modes )
			{
				if ( m.equals( mode ) )
				{
					contained = true;
					break;
				}
			}
			if ( !contained )
			{
				modes.add( mode );
			}
		}

		Collections.sort( modes, new Comparator<String>()
		{

			@Override
			public int compare( String s1, String s2 )
			{
				int split = s1.indexOf( "x" );
				int rX1 = Integer.parseInt( s1.substring( 0, split ) );

				split = s2.indexOf( "x" );
				int rX2 = Integer.parseInt( s2.substring( 0, split ) );

				if ( rX1 < rX2 ) return -1;
				else if ( rX1 > rX2 ) return 1;
				return 0;
			}

		} );

		String[] m = new String[modes.size()];

		return modes.toArray( m );
	}

	public void setDefaultPrefs( Preferences prefs )
	{
		prefs.putBoolean( "pathfindMovement", false );

		prefs.putFloat( "musicVolume", 1 );
		prefs.putFloat( "ambientVolume", 1 );
		prefs.putFloat( "effectVolume", 1 );

		prefs.putInteger( "resolutionX", 800 );
		prefs.putInteger( "resolutionY", 600 );
		prefs.putBoolean( "fullscreen", false );
		prefs.putBoolean( "borderless", false );
		prefs.putBoolean( "vSync", true );
		prefs.putInteger( "fps", 0 );
		prefs.putFloat( "animspeed", 1 );
		prefs.putInteger( "msaa", 16 );

		GlobalData.Global.controls.defaultArrow();
		for ( Controls.Keys key : Controls.Keys.values() )
		{
			prefs.putInteger( key.toString(), GlobalData.Global.controls.getKeyCode( key ) );
		}
	}

	@Override
	public void setToNativeResolution( Preferences prefs )
	{
		DisplayMode dm = Gdx.graphics.getDisplayMode();

		prefs.putInteger( "resolutionX", dm.width );
		prefs.putInteger( "resolutionY", dm.height );

		updateApplication( prefs );
	}

}