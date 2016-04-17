package com.lyeeedar.Quests.Input

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 24-Jan-16.
 */
class QuestInputFlagEquals : AbstractQuestInput()
{
	lateinit var key: String
	lateinit var value: String
	var not: Boolean = false

	override fun evaluate(): Boolean
	{
		if (GlobalData.Global.questManager.flags.containsKey(key))
		{
			val `val` = GlobalData.Global.questManager.flags.get(key)

			return !not && `val`.equals(value, ignoreCase = true)
		}
		else
		{
			return false
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key").toLowerCase()
		value = xml.get("Value").toLowerCase()


		not = xml.name == "FlagNotEquals"
	}
}
