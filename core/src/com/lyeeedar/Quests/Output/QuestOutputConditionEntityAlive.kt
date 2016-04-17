package com.lyeeedar.Quests.Output

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.NameComponent
import com.lyeeedar.Components.name
import com.lyeeedar.Components.stats
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 17-Apr-16.
 */

class QuestOutputConditionEntityAlive(): AbstractQuestOutputCondition()
{
	lateinit var name: String
	var not: Boolean = false
	lateinit var entities: ImmutableArray<Entity>

	override fun evaluate(): Boolean
	{
		entities = GlobalData.Global.engine.getEntitiesFor(Family.all(NameComponent::class.java).get())

		var lives: Boolean = false
		for (entity in entities)
		{
			if (entity.name() == name)
			{
				if (entity.stats()?.hp ?: 1f > 0f)
				{
					lives = true
					break
				}
			}
		}

		return lives != not
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.text.toLowerCase()
		not = xml.name != "EntityAlive"
	}
}