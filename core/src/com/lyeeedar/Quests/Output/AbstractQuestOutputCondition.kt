package com.lyeeedar.Quests.Output

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.badlogic.gdx.utils.reflect.ReflectionException
import com.lyeeedar.Quests.Input.AbstractQuestInput
import com.lyeeedar.Quests.Input.QuestInputFlagEquals

import java.util.HashMap

/**
 * Created by Philip on 23-Jan-16.
 */
abstract class AbstractQuestOutputCondition
{
	abstract fun evaluate(): Boolean
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{

		fun load(xml: XmlReader.Element): AbstractQuestOutputCondition
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractQuestOutputCondition

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractQuestOutputCondition>
		{
			val type = when(name) {
				"ENTITYALIVE", "ENTITYDEAD", "ENTITYNOTALIVE" -> QuestOutputConditionEntityAlive::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid quest output condition type: $name")
			}

			return type
		}
	}
}
