package com.lyeeedar

import android.content.SharedPreferences
import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.backends.android.AndroidPreferences
import com.lyeeedar.AbstractApplicationChanger
import com.lyeeedar.Global

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * Created by Philip on 20-Jan-16.
 */
class AndroidApplicationChanger : AbstractApplicationChanger(Gdx.app.getPreferences("game-settings"))
{

	override fun createApplication(game: MainGame, pref: Preferences): Application?
	{
		return null
	}

	override fun processResources()
	{
	}

	override fun updateApplication(pref: Preferences)
	{
		val width = pref.getInteger("resolutionX")
		val height = pref.getInteger("resolutionY")
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

		if (displayModes.size == 1)
		{
			val mode = (displayModes[0].width / 2).toString() + "x" + (displayModes[0].height / 2).toString()
			modes.add(mode)
		}

		modes.add("480x360")
		modes.add("800x600")

		Collections.sort(modes, Comparator<String> { s1, s2 ->
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

	override fun setToNativeResolution(prefs: Preferences)
	{

	}

	override fun setDefaultPrefs(prefs: Preferences)
	{
		prefs.putBoolean("pathfindMovement", false)

		prefs.putFloat("musicVolume", 1f)
		prefs.putFloat("ambientVolume", 1f)
		prefs.putFloat("effectVolume", 1f)

		prefs.putInteger("resolutionX", 360)
		prefs.putInteger("resolutionY", 640)
		prefs.putInteger("fps", 30)
		prefs.putFloat("animspeed", 1f)
	}
}
