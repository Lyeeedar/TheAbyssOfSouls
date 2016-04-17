package com.lyeeedar.Quests.Input

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection

/**
 * Created by Philip on 17-Apr-16.
 */

abstract class AbstractQuestInput()
{
	abstract fun evaluate(): Boolean
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractQuestInput
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractQuestInput

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractQuestInput>
		{
			val type = when(name) {
				"FLAGEQUALS", "FLAGNOTEQUALS" -> QuestInputFlagEquals::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid quest input type: $name")
			}

			return type
		}
	}
}