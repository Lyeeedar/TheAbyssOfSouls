package com.lyeeedar.Quests


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Rarity

import java.io.IOException
import java.util.Random

/**
 * Created by Philip on 23-Jan-16.
 */
class QuestManager
{
	var availableQuests = Array<Quest>()

	var flags = ObjectMap<String, String>()

	var difficulty = 1
	var count = 0

	//public SaveLevel currentLevel;

	var seed: Int = 0

	lateinit var currentQuest: Quest

	init
	{
		val reader = XmlReader()
		var xml: XmlReader.Element = reader.parse(Gdx.files.internal("Quests/QuestList.xml"))

		for (i in 0..xml.childCount - 1)
		{
			val questEl = xml.getChild(i)
			val quest = Quest.load(questEl.text)
			availableQuests.add(quest)
		}
	}

	val quests: Array<Quest>
		get()
		{
			val ran = Random(seed.toLong())

			val validQuests = Array<Quest>()
			for (quest in availableQuests)
			{
				if (Math.abs(quest.difficulty - difficulty) <= 1 && quest.evaluateInputs())
				{
					val rarity = Rarity.Values.size - quest.rarity.ordinal + 1
					for (i in 0..rarity - 1)
					{
						validQuests.add(quest)
					}
				}
			}

			if (validQuests.size == 0)
			{
				throw RuntimeException("No Valid quests! For difficulty " + difficulty)
			}

			val chosen = Array<Quest>()

			val count = 3 + ran.nextInt(2)
			for (i in 0..count - 1)
			{
				val picked = validQuests.get(ran.nextInt(validQuests.size))
				chosen.add(picked)

				val itr = validQuests.iterator()
				while (itr.hasNext())
				{
					if (itr.next() == picked)
					{
						itr.remove()
					}
				}

				if (validQuests.size == 0)
				{
					break
				}
			}

			return chosen
		}
}
