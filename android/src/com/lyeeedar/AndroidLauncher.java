package com.lyeeedar;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.resolutionStrategy.calcMeasures(360, 640);
		config.disableAudio = false;

		Global.game = new MainGame();

		initialize( Global.game, config );

		Global.applicationChanger = new AndroidApplicationChanger();
		Global.applicationChanger.updateApplication( Global.applicationChanger.prefs );
	}
}
