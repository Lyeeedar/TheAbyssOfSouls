package com.lyeeedar.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.lyeeedar.GlobalData;
import com.lyeeedar.MainGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GlobalData.Global.release = true;
		GlobalData.Global.game = new MainGame();
		GlobalData.Global.applicationChanger = new LwjglApplicationChanger();
		GlobalData.Global.applicationChanger.createApplication();
	}
}
