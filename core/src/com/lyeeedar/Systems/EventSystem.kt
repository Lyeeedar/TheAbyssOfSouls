package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Events.EventArgs

/**
 * Created by Philip on 22-Mar-16.
 */

class EventSystem(): IteratingSystem(Family.all(EventComponent::class.java).get(), 2)
{
	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		if (entity == null) return

		val eventData = Mappers.event.get(entity)
		eventData.pendingEvents.add(EventArgs(EventComponent.EventType.TURN, entity, entity, deltaTime))

		while (eventData.pendingEvents.size > 0)
		{
			val event = eventData.pendingEvents.removeIndex(0)

			for (handler in eventData.handlers.get(event.type))
			{
				handler.handle(event)
			}
		}
	}
}