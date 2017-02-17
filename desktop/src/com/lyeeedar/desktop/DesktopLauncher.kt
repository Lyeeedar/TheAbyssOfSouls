package com.lyeeedar.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.lyeeedar.Global
import com.lyeeedar.MainGame

object DesktopLauncher
{
	@JvmStatic fun main(arg: Array<String>)
	{
		//Global.release = true
		Global.game = MainGame()
		Global.applicationChanger = LwjglApplicationChanger()
		Global.applicationChanger.createApplication()
	}
}
