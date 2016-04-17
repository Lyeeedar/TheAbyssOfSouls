package com.lyeeedar.Quests.Input

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 23-Jan-16.
 */
class QuestInputFlagPresent : AbstractQuestInput()
{
	lateinit var key: String
	var not: Boolean = false

	override fun evaluate(): Boolean
	{
		if (GlobalData.Global.questManager.flags.containsKey(key))
		{
			return !not
		}

		return not
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.text.toLowerCase()
		not = xml.name == "FlagNotPresent"
	}
}
