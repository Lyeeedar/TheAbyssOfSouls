package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Screens.GameScreen
import com.lyeeedar.UI.lamda
import com.lyeeedar.Util.*
import ktx.actors.alpha
import ktx.actors.plus
import ktx.actors.then
import ktx.collections.set

class World
{
	val levels = ObjectMap<String, LevelData>()

	lateinit var root: LevelData
	lateinit var currentLevel: LevelData

	val globalVariables = ObjectFloatMap<String>()

	fun reload()
	{
		levels.clear()
		globalVariables.clear()

		val xml = getXml("Grammars/World")

		val levelsEl = xml.getChildByName("Levels")
		for (el in levelsEl.children())
		{
			val level = LevelData.load(el)
			levels[el.getAttribute("GUID")] = level
		}

		val rootKey = xml.get("Root")
		root = levels[rootKey]

		currentLevel = root
	}

	init
	{
		reload()
	}

	fun changeLevel(key: String, type: String, lastPlayer: Entity, fadeColour: Colour)
	{
		val level = levels[currentLevel.connections[key]]

		Global.pause = true

		val fadeTable = Table()
		fadeTable.background = TextureRegionDrawable(AssetManager.loadTextureRegion("Sprites/white.png")).tint(fadeColour.color())
		fadeTable.alpha = 0f

		val sequence = Actions.alpha(0f) then Actions.fadeIn(1f) then lamda {
			currentLevel = level

			GameScreen.instance.loadLevel(level, type, lastPlayer)

		} then Actions.fadeOut(1f) then lamda { Global.pause = false } then Actions.removeActor()

		fadeTable + sequence

		Global.stage.addActor(fadeTable)
		fadeTable.setFillParent(true)
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
	var seed: Long = 0
	val connections = ObjectMap<String, String>()
	var seenGrid: Array2D<Boolean> = Array2D(0, 0) { x, y -> false }

	companion object
	{
		fun load(xml: XmlReader.Element): LevelData
		{
			val level = LevelData()
			level.grammar = xml.get("Grammar")
			level.seed = Random.random.nextLong()

			val connectionsEl = xml.getChildByName("Connected")
			if (connectionsEl != null)
			{
				for (el in connectionsEl.children())
				{
					val key = el.get("Key")
					val next = el.get("Next")

					level.connections[key] = next
				}
			}

			return level
		}
	}
}
