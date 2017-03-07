package com.lyeeedar.Level

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Screens.GameScreen
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getXml
import ktx.collections.set

class World
{
	lateinit var root: LevelData

	lateinit var currentLevel: LevelData

	init
	{
		val xml = getXml("Grammars/World")
		val rootEl = xml.getChildByName("Root")
		root = LevelData.load(rootEl)

		currentLevel = root
	}

	fun changeLevel(level: LevelData)
	{
		currentLevel = level

		GameScreen.instance.loadLevel(level)
	}

	companion object
	{
		val world = World()
	}
}

class LevelData
{
	// insert kryo save here

	lateinit var grammar: String
	var parent: LevelData? = null
	val connections = ObjectMap<String, LevelData>()

	companion object
	{
		fun load(xml: XmlReader.Element): LevelData
		{
			val level = LevelData()
			level.grammar = xml.get("Grammar")

			val connectionsEl = xml.getChildByName("Connected")
			if (connectionsEl != null)
			{
				for (el in connectionsEl.children())
				{
					val key = el.get("Key")
					val next = el.getChildByName("Next")
					val nlevel = load(next)
					nlevel.parent = level

					level.connections[key] = nlevel
				}
			}

			return level
		}
	}
}
