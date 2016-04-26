package com.lyeeedar.Sound

import java.io.IOException
import java.util.HashSet

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Components.TaskComponent
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Pathfinding.AStarPathfind
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import squidpony.squidgrid.SoundMap

class SoundInstance
{
	enum class Function
	{
		LINEAR,
		LOG,
		LOGREVERSE,
		INVERSE
	}

	lateinit var sound: Sound
	lateinit var name: String
	lateinit var groupName: String

	var pitch = 1.5f
	var volume = 1f
	var function = Function.LINEAR
	var rangeMin = 2
	var rangeMax = 10

	var shoutFaction: ObjectSet<String>? = null
	var key: String? = null
	var value: Any? = null

	constructor()
	{

	}

	constructor(sound: Sound, group: String)
	{
		this.sound = sound
		this.groupName = group
	}

	fun copy(): SoundInstance
	{
		val soundInstance = SoundInstance()
		soundInstance.sound = sound
		soundInstance.name = name
		soundInstance.groupName = groupName

		soundInstance.pitch = pitch
		soundInstance.volume = volume
		soundInstance.rangeMin = rangeMin
		soundInstance.rangeMax = rangeMax
		soundInstance.function = function

		soundInstance.shoutFaction = shoutFaction
		soundInstance.key = key
		soundInstance.value = value

		return soundInstance
	}

	fun play(tile: Tile)
	{
		// calculate data propogation

		val soundMap = SoundMap(tile.level.charGrid)
		soundMap.setSound(tile.x, tile.y, rangeMax.toDouble() + 1)
		val output = soundMap.scan()

		if (key != null)
		{
			for (x in 0..tile.level.width - 1)
			{
				for (y in 0..tile.level.height - 1)
				{
					if (output[x][y] > 0)
					{
						val t = tile.level.getTile(x, y) ?: continue
						for (e in t.contents)
						{
							val task = Mappers.task.get(e)
							if (task != null)
							{
								task.ai.setData(key!!, value)
							}
						}
					}
				}
			}
		}

		val playerPos = Mappers.position.get(tile.level.player)
		val playerDist = output[playerPos.position.x][playerPos.position.y]

		// calculate sound play volume
		if (playerDist > 0)
		{
			val dist = rangeMax - (playerDist - 1)
			val alpha = 1f - (dist - rangeMin).toFloat() / (rangeMax - rangeMin).toFloat()

			val vol: Float = if (dist > rangeMin)
				when(function) {
					Function.LOG -> 1f - (1f - alpha) * (1f - alpha) * (1f - alpha) * (1f - alpha)
					Function.LOGREVERSE -> (1f - alpha) * (1f - alpha) * (1f - alpha) * (1f - alpha)
					Function.INVERSE -> ( rangeMax.toFloat() / rangeMin.toFloat() ) * ( 0.02f / ( dist.toFloat() / rangeMax.toFloat() ) )
					else -> alpha
				} else 1f

			if (vol > 0f)
			{
				val actualVolume = vol * volume

				var pan = 0f
				if (tile.x != playerPos.x)
				{
					pan = (tile.x - playerPos.x).toFloat() / rangeMax.toFloat()
				}

				val group = SoundGroup.groups[groupName]
				group.play(sound, actualVolume, pitch, pan)
			}
		}
	}

	companion object
	{

		@JvmStatic fun load(xml: Element): SoundInstance
		{
			val sound = SoundInstance()
			sound.name = xml.get("Name")
			sound.groupName = xml.get("Group")
			sound.sound = AssetManager.loadSound(sound.name)

			sound.rangeMin = xml.getInt("RangeMin")
			sound.rangeMax = xml.getInt("RangeMax")
			sound.volume = xml.getFloat("Volume", sound.volume)
			sound.pitch = xml.getFloat("Pitch", sound.pitch)
			sound.function = Function.valueOf(xml.get("Function", "Linear").toUpperCase())

			return sound
		}

		private val soundMap = ObjectMap<String, Element>()
		private var loaded = false
		@JvmStatic fun getSound(name: String): SoundInstance
		{
			if (!loaded)
			{
				loaded = true

				val reader = XmlReader()
				var xml: Element? = null

				try
				{
					xml = reader.parse(Gdx.files.internal("Sounds/SoundMap.xml"))
				}
				catch (e: IOException)
				{
					e.printStackTrace()
				}

				for (i in 0..xml!!.childCount - 1)
				{
					val el = xml.getChild(i)
					soundMap.put(el.name, el)
				}
			}

			if (soundMap.containsKey(name))
			{
				return SoundInstance.load(soundMap.get(name))
			}
			else
			{
				val sound = SoundInstance()
				sound.name = name
				sound.sound = AssetManager.loadSound(name)
				return sound
			}
		}
	}
}
