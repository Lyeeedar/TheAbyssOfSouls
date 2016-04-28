package com.lyeeedar.Quests

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader

import com.lyeeedar.DungeonGeneration.Data.SymbolicRoomData
import com.lyeeedar.GlobalData
import com.lyeeedar.MainGame
import com.lyeeedar.Quests.Input.AbstractQuestInput
import com.lyeeedar.Quests.Output.QuestOutputGroup
import com.lyeeedar.Rarity
import com.lyeeedar.Screens.LoadingScreen
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.Seperator

import java.io.IOException

/**
 * Created by Philip on 23-Jan-16.
 */
class Quest
{
	lateinit var name: String
	lateinit var description: String
	lateinit var icon: Sprite
	var reward: Int = 0
	lateinit var faction: String
	lateinit var level: String
	lateinit var levelText: String
	var difficulty: Int = 0
	lateinit var music: String
	lateinit var rarity: Rarity

	lateinit var path: String
	var inputs = Array<AbstractQuestInput>()
	var outputs = Array<QuestOutputGroup>()
	var rooms = Array<SymbolicRoomData>()

	fun evaluateInputs(): Boolean
	{
		for (input in inputs)
		{
			if (!input.evaluate())
			{
				return false
			}
		}

		return true
	}

	fun evaluateOutputs()
	{
		for (output in outputs)
		{
			val succeed = output.evaluate()

			if (succeed)
			{
				// do stuff?
			}
		}
	}

	fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name")
		description = xml.get("Description")
		icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
		reward = xml.getInt("Reward")
		faction = xml.get("Faction")
		level = xml.get("Level")
		levelText = xml.get("LevelText")
		difficulty = xml.getInt("Difficulty")
		music = xml.get("Music")
		rarity = Rarity.valueOf(xml.get("Rarity", "COMMON").toUpperCase())

		val inputsElement = xml.getChildByName("Inputs")
		if (inputsElement != null)
		{
			for (i in 0..inputsElement.childCount - 1)
			{
				val inputElement = inputsElement.getChild(i)
				val input = AbstractQuestInput.load(inputElement)
				inputs.add(input)
			}
		}

		val outputsElement = xml.getChildByName("Outputs")
		if (outputsElement != null)
		{
			for (i in 0..outputsElement.childCount - 1)
			{
				val outputElement = outputsElement.getChild(i)
				val output = QuestOutputGroup()
				output.parse(outputElement)

				outputs.add(output)
			}
		}

		val roomsElement = xml.getChildByName("Rooms")
		for (i in 0..roomsElement.childCount - 1)
		{
			val roomElement = roomsElement.getChild(i)

			val count = roomElement.getIntAttribute("Count", 1)
			for (ii in 0..count - 1)
			{
				val room = SymbolicRoomData.load(roomElement)

				rooms.add(room)
			}
		}
	}

	private val entrance: SymbolicRoomData
		get()
		{
			val reader = XmlReader()
			var xml: XmlReader.Element = reader.parse(Gdx.files.internal("Levels/Ship.xml"))

			val room = SymbolicRoomData.load(xml)
			return room
		}

	fun createLevel()
	{
		val requiredRooms = Array<SymbolicRoomData>()
		requiredRooms.add(entrance)
		requiredRooms.addAll(rooms)

		//SaveLevel level = new SaveLevel( name, requiredRooms, Global.QuestManager.seed );
		//Global.QuestManager.currentLevel = level;

		LoadingScreen.Instance.load(level)
		GlobalData.Global.game.switchScreen(MainGame.ScreenEnum.LOADING)
	}

	fun createTable(skin: Skin): Table
	{
		val table = Table()

		table.defaults().pad(10f)

		table.add(Label(name, skin, "title")).expandX().left()
		table.row()

		table.add(Seperator(skin)).expandX().fillX()
		table.row()

		val desc = Label(description, skin)
		desc.setWrap(true)
		table.add(desc).expandX().fillX().left()
		table.row()

		table.add(Label("Location: " + levelText, skin)).expandX().fillX().left()
		table.row()

		table.add(Label("Threat Level: " + difficulty, skin)).expandX().fillX().left()
		table.row()

		val rew = Label("Reward: " + reward, skin)
		rew.color = Color.GOLD

		table.add(rew).expandX().left()
		table.row()

		return table
	}

	companion object
	{

		fun load(name: String): Quest
		{
			val quest = Quest()
			quest.path = name

			val reader = XmlReader()
			var xml: XmlReader.Element = reader.parse(Gdx.files.internal("Quests/$name.xml"))

			quest.parse(xml)

			return quest
		}
	}
}
