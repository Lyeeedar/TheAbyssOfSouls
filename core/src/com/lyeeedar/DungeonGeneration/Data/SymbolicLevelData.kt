package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicLevelData()
{
	val symbolMap: ObjectMap<Char, Symbol> = ObjectMap()
	val rooms: com.badlogic.gdx.utils.Array<SymbolicRoomData> = com.badlogic.gdx.utils.Array()
	val roomGenerators: com.badlogic.gdx.utils.Array<XmlReader.Element> = com.badlogic.gdx.utils.Array()
	val factions: com.badlogic.gdx.utils.Array<FactionData> = com.badlogic.gdx.utils.Array()
	var preprocessor: XmlReader.Element? = null
	var corridor: SymbolicCorridorData = SymbolicCorridorData()
	var levelTheme: RoomTheme? = null

	companion object
	{
		fun load(xml: XmlReader.Element): SymbolicLevelData
		{
			val level = SymbolicLevelData()

			val preEl = xml.getChildByName("Preprocessor")
			if (preEl != null)
			{
				level.preprocessor = preEl.getChild(0)
			}

			val symbols = xml.getChildByName("Symbols")
			if (symbols != null)
			{
				for (i in 0..symbols.childCount-1)
				{
					val el = symbols.getChild(i)
					val symbol = Symbol.load(el)

					level.symbolMap.put(symbol.char, symbol)
				}
			}

			val rooms = xml.getChildByName("Rooms")
			if (rooms != null)
			{
				for (i in 0..rooms.childCount - 1)
				{
					val el = rooms.getChild(i)
					val room = SymbolicRoomData.load(el)

					level.rooms.add(room)
				}
			}

			val factions = xml.getChildByName("Factions")
			if (factions != null)
			{
				for (i in 0..factions.childCount-1)
				{
					val el = factions.getChild(i)
					val faction = FactionData.load(el)

					level.factions.add(faction)
				}
			}

			val generators = xml.getChildByName("Generators")
			if (generators != null)
			{
				for (i in 0..generators.childCount-1)
				{
					val el = generators.getChild(i)

					level.roomGenerators.add(el)
				}
			}

			val corridorEl = xml.getChildByName("Corridor")
			if (corridorEl != null)
			{
				level.corridor = SymbolicCorridorData.load(corridorEl)
			}

			val themeEl = xml.getChildByName("Theme")
			if (themeEl != null)
			{
				level.levelTheme = RoomTheme.load(themeEl)
			}

			return level
		}
	}
}
