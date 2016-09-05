package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.*
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Level.Tile

/**
 * Created by Philip on 22-Mar-16.
 */

class EventSystem(): IteratingSystem(Family.all(EventComponent::class.java).get(), systemList.indexOf(EventSystem::class))
{
	fun processEntity(entity: Entity?)
	{
		processEntity(entity, 0f)
	}

	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		if (entity == null) return
		val pos = entity.pos() ?: return
		val tile = entity.tile() ?: return
		val eventData = Mappers.event.get(entity) ?: return

		eventData.pendingEvents.add(EventArgs(EventComponent.EventType.TURN, entity, entity, deltaTime))

		while (eventData.pendingEvents.size > 0)
		{
			val event = eventData.pendingEvents.removeIndex(0)

			for (handler in eventData.handlers.get(event.type))
			{
				handle(pos, handler, event, tile)
			}
		}
	}

	fun handle(pos: PositionComponent, handler: EventActionGroup, event: EventArgs, tile: Tile)
	{
		for (x in (pos.min.x-handler.aoe)..(pos.max.x+handler.aoe))
		{
			for (y in (pos.min.y-handler.aoe)..(pos.max.y+handler.aoe))
			{
				handler.handle(event, tile.level.getTile(x, y) ?: continue)
			}
		}
	}
}