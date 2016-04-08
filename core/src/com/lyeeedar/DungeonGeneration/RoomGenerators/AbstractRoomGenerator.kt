package com.lyeeedar.DungeonGeneration.RoomGenerators

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection

/**
 * Created by Philip on 08-Apr-16.
 */

abstract class AbstractRoomGenerator()
{
	abstract fun parse(xml: XmlReader.Element)

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