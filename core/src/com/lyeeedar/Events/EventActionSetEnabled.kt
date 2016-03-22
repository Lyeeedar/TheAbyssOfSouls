package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionSetEnabled(group: EventActionGroup): NameRestrictedEventAction(group, Family.all(EventComponent::class.java).get())
{
	var actionName: String? = null
	var enabled: Boolean = true

	override fun handle(args: EventArgs, entity: Entity)
	{
		if (actionName == null)
		{
			group.enabled = enabled
		}
		else
		{
			super.handle(args, entity)
		}
	}

	override fun handleEntity(args: EventArgs, entity: Entity)
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

	override fun parse(xml: XmlReader.Element)
	{
		enabled = xml.name.toUpperCase() == "ENABLE"
		entityName = xml.getAttribute("Entity", "")
		actionName = xml.text
	}
}