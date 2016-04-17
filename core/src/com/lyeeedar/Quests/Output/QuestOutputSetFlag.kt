package com.lyeeedar.Quests.Output

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 17-Apr-16.
 */

class QuestOutputSetFlag(): AbstractQuestOutput()
{
	lateinit var key: String
	lateinit var value: String

	constructor(key: String, value: String)
		: this()
	{
		this.key = key
		this.value = value
	}

	override fun evaluate()
	{
		GlobalData.Global.questManager.flags.put(key, value)
	}

	override fun parse(xml: XmlReader.Element)
	{
		if (xml.name != "SetFlag")
		{
			key = xml.name.substring(2).toLowerCase()
			value = xml.text.toLowerCase()
		}
		else
		{
			key = xml.get("Key").toLowerCase()
			value = xml.get("Value", "1").toLowerCase()
		}
	}
}