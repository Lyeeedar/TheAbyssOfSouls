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
	var preprocessor: XmlReader.Element? = null
	var corridor: SymbolicCorridorData = SymbolicCorridorData()

	companion object
	{
		fun load(xml: XmlReader.Element): SymbolicLevelData
		{
			val level = SymbolicLevelData()

			level.preprocessor = xml.getChildByName("Preprocessor").getChild(0)

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
					val el = symbols.getChild(i)
					val room = SymbolicRoomData.load(el)

					level.rooms.add(room)
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

			return level
		}
	}
}
