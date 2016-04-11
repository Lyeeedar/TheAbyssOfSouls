package com.lyeeedar.DungeonGeneration.RoomGenerators

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.DungeonGeneration.Data.Symbol
import com.lyeeedar.DungeonGeneration.Data.SymbolicRoom
import com.lyeeedar.Util.Array2D
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

abstract class AbstractRoomGenerator(val ensuresConnectivity: Boolean)
{
	abstract fun parse(xml: XmlReader.Element)
	abstract fun process(grid: Array2D<Symbol>, symbolMap: ObjectMap<Char, Symbol>, ran: Random)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractRoomGenerator
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c, AbstractRoomGenerator::class.java).newInstance() as AbstractRoomGenerator

			return instance
		}

		fun getClass(name: String): Class<out AbstractRoomGenerator>
		{
			val type = when(name) {
				//"ENABLE", "DISABLE" -> EventActionSetEnabled::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid room generator type: $name")
			}

			return type
		}
	}
}