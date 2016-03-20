package com.lyeeedar;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lyeeedar.Screens.AbstractScreen;
import com.lyeeedar.Screens.GameScreen;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class MainGame extends Game
{
	public static MainGame Instance;

	public MainGame()
	{
		Instance = this;
	}

	public enum ScreenEnum
	{
		GAME
	}

	public final HashMap<ScreenEnum, AbstractScreen> screens = new HashMap<ScreenEnum, AbstractScreen>();

	@Override
	public void create()
	{
		GlobalData.Global.applicationChanger.processResources();
		GlobalData.Global.setup();

		if (!GlobalData.Global.android)
		{
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter( sw );

			final Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler()
			{
				public void uncaughtException(Thread myThread, Throwable e)
				{
					e.printStackTrace( pw );
					String exceptionAsString = sw.toString();

					FileHandle file = Gdx.files.local( "error.log" );
					file.writeString( exceptionAsString, false );

					JOptionPane.showMessageDialog( null, "An fatal error occured. Please send error.log to me so that I can fix it.", "An error occured", JOptionPane.ERROR_MESSAGE );

					e.printStackTrace();
				}
			};

			Thread.currentThread().setDefaultUncaughtExceptionHandler(handler);
		}

		screens.put( ScreenEnum.GAME, new GameScreen() );

		switchScreen( ScreenEnum.GAME );
	}

	public void switchScreen( ScreenEnum screen )
	{
		this.setScreen( screens.get( screen ) );
	}
}
