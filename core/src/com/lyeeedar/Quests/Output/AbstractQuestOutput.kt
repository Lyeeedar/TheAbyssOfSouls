package com.lyeeedar.Quests.Output

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection

/**
 * Created by Philip on 17-Apr-16.
 */

abstract class AbstractQuestOutput()
{
	abstract fun evaluate()
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{

		fun load(xml: XmlReader.Element): AbstractQuestOutput
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractQuestOutput

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractQuestOutput>
		{
			val type = when(name) {
			 "SETFLAG" -> QuestOutputSetFlag::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid quest output type: $name")
			}

			return type
		}
	}
}