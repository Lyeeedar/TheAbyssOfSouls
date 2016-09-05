package com.lyeeedar.desktop

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import com.badlogic.gdx.Application
import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics.DisplayMode
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences
import com.lyeeedar.AbstractApplicationChanger
import com.lyeeedar.Global
import com.lyeeedar.MainGame

class LwjglApplicationChanger : AbstractApplicationChanger(LwjglPreferences("game-settings", "settings"))
{
	var prefs: Preferences? = null

	override fun createApplication(game: MainGame, pref: Preferences): Application
	{
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "" + pref.getBoolean("borderless"))

		val cfg = LwjglApplicationConfiguration()

		cfg.title = "MHRL"
		cfg.width = pref.getInteger("resolutionX")
		cfg.height = pref.getInteger("resolutionY")
		cfg.fullscreen = pref.getBoolean("fullscreen")
		cfg.vSyncEnabled = pref.getBoolean("vSync")
		cfg.foregroundFPS = 0
		cfg.backgroundFPS = 0
		cfg.samples = pref.getInteger("msaa")
		cfg.addIcon("Sprites/Unpacked/Icon32.png", FileType.Internal)
		cfg.allowSoftwareMode = true

		Global.fps = pref.getInteger("fps")
		Global.resolution[0] = pref.getInteger("resolutionX").toFloat()
		Global.resolution[1] = pref.getInteger("resolutionY").toFloat()

		return LwjglApplication(game, cfg)
	}

	override fun processResources()
	{
		if (!Global.release)
		{
			AtlasCreator()
		}
	}

	override fun updateApplication(pref: Preferences)
	{
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "" + pref.getBoolean("borderless"))

		val width = pref.getInteger("resolutionX")
		val height = pref.getInteger("resolutionY")
		val fullscreen = pref.getBoolean("fullscreen")

		Global.fps = pref.getInteger("fps")

		if (fullscreen)
		{
			val mode = Gdx.graphics.displayMode
			Gdx.graphics.setFullscreenMode(mode)
		} else
		{
			Gdx.graphics.setWindowedMode(width, height)
		}
	}

	override fun getSupportedDisplayModes(): Array<String>
	{
		val displayModes = Gdx.graphics.displayModes

		val modes = ArrayList<String>()

		for (i in displayModes.indices)
		{
			val mode = displayModes[i].width.toString() + "x" + displayModes[i].height.toString()

			var contained = false
			for (m in modes)
			{
				if (m == mode)
				{
					contained = true
					break
				}
			}
			if (!contained)
			{
				modes.add(mode)
			}
		}

		Collections.sort(modes, Comparator<kotlin.String> { s1, s2 ->
			var split = s1.indexOf("x")
			val rX1 = Integer.parseInt(s1.substring(0, split))

			split = s2.indexOf("x")
			val rX2 = Integer.parseInt(s2.substring(0, split))

			if (rX1 < rX2)
				return@Comparator -1
			else if (rX1 > rX2) return@Comparator 1
			0
		})

		val m = arrayOfNulls<String>(modes.size)

		return modes.toTypedArray()
	}

	override fun setDefaultPrefs(prefs: Preferences)
	{
		prefs.putBoolean("pathfindMovement", false)

		prefs.putFloat("musicVolume", 1f)
		prefs.putFloat("ambientVolume", 1f)
		prefs.putFloat("effectVolume", 1f)

		prefs.putInteger("resolutionX", 600)
		prefs.putInteger("resolutionY", 400)
		prefs.putBoolean("fullscreen", false)
		prefs.putBoolean("borderless", false)
		prefs.putBoolean("vSync", true)
		prefs.putInteger("fps", 0)
		prefs.putFloat("animspeed", 1f)
		prefs.putInteger("msaa", 16)
	}

	override fun setToNativeResolution(prefs: Preferences)
	{
		val dm = Gdx.graphics.displayMode

		prefs.putInteger("resolutionX", dm.width)
		prefs.putInteger("resolutionY", dm.height)

		updateApplication(prefs)
	}

}