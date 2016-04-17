package com.lyeeedar.Quests.Output

import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 17-Apr-16.
 */

class QuestOutputGroup()
{
	val conditions: com.badlogic.gdx.utils.Array<AbstractQuestOutputCondition> = com.badlogic.gdx.utils.Array()
	val outputs: com.badlogic.gdx.utils.Array<AbstractQuestOutput> = com.badlogic.gdx.utils.Array()

	fun evaluate(): Boolean
	{
		for (cond in conditions)
		{
			if (!cond.evaluate())
			{
				return false
			}
		}

		for (out in outputs)
		{
			out.evaluate()
		}

		return true
	}

	fun parse(xml: XmlReader.Element)
	{
		// has conditions
		val condEl = xml.getChildByName("Conditions")
		if (condEl != null)
		{
			for (i in 0..condEl.childCount-1)
			{
				val el = condEl.getChild(i)
				val cond = AbstractQuestOutputCondition.load(el)
				conditions.add(cond)
			}
		}

		val outEl = xml.getChildByName("Outputs")
		if (outEl != null)
		{
			for (i in 0..outEl.childCount-1)
			{
				val el = outEl.getChild(i)
				val out = AbstractQuestOutput.load(el)
				outputs.add(out)
			}
		}
		else
		{
			val value = xml.get("Value", "1").toLowerCase()
			outputs.add(QuestOutputSetFlag(xml.name.toLowerCase(), value))
		}
	}
}