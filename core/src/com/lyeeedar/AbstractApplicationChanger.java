package com.lyeeedar;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public abstract class AbstractApplicationChanger
{
	public Preferences prefs;

	public AbstractApplicationChanger( Preferences prefs )
	{
		this.prefs = prefs;
		if ( !prefs.getBoolean( "created" ) )
		{
			setDefaultPrefs( prefs );
			prefs.putBoolean( "created", true );

			prefs.flush();
		}
	}

	public void createApplication()
	{
		if ( Gdx.app != null )
		{
			System.err.println( "Application already exists!" );
			return;
		}

		Gdx.app = createApplication( Global.game, prefs );
	}

	public abstract void processResources();

	public abstract void setDefaultPrefs( Preferences prefs );

	public abstract Application createApplication( MainGame game, Preferences pref );

	public abstract void updateApplication( Preferences pref );

	public abstract String[] getSupportedDisplayModes();

	public abstract void setToNativeResolution( Preferences prefs );
}
