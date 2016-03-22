package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.StatisticsComponent
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionSetEnabled(): AbstractEventAction()
{
	val statEntities: ImmutableArray<Entity> = GlobalData.Global.engine?.getEntitiesFor(Family.all(StatisticsComponent::class.java, EventComponent::class.java).get()) ?: throw RuntimeException("Engine null")
	val entities: ImmutableArray<Entity> = GlobalData.Global.engine?.getEntitiesFor(Family.all(EventComponent::class.java).get()) ?: throw RuntimeException("Engine null")

	lateinit var entityName: String
	lateinit var actionName: String
	var enabled: Boolean = true

	override fun handle(args: EventArgs)
	{
		if (entityName.length > 0)
		{
			if (entityName == "this")
			{
				val event = Mappers.event.get(args.receiver)
				for (type in EventComponent.EventType.values())
				{
					for (e in event.handlers.get(type))
					{
						if (e.name == actionName)
						{
							e.enabled = enabled
						}
					}
				}
			}
			else
			{
				for (entity in statEntities)
				{
					val stat = Mappers.stats.get(entity)
					val event = Mappers.event.get(entity)

					if (stat.name == entityName)
					{
						for (type in EventComponent.EventType.values())
						{
							for (e in event.handlers.get(type))
							{
								if (e.name == actionName)
								{
									e.enabled = enabled
								}
							}
						}
					}
				}
			}
		}
		else
		{
			for (entity in entities)
			{
				val event = Mappers.event.get(entity)
				for (type in EventComponent.EventType.values())
				{
					for (e in event.handlers.get(type))
					{
						if (e.name == actionName)
						{
							e.enabled = enabled
						}
					}
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		enabled = xml.getBooleanAttribute("Enabled", true)
		entityName = xml.getAttribute("Entity", "")
		actionName = xml.text
	}
}