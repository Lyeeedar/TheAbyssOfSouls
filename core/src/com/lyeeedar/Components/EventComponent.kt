package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 22-Mar-16.
 */

class EventComponent: Component
{
	constructor()
	{
		for (type in EventType.values())
		{
			handlers.put(type, com.badlogic.gdx.utils.Array<EventActionGroup>())
		}
	}

	enum class EventType
	{
		TURN,
		MOVE,
		ATTACK,
		WAIT,

		DEAL_DAMAGE,
		RECEIVE_DAMAGE,
		HEAL,

		SPAWN,
		DEATH,

		ACTIVATE,
		NONE
	}

	val handlers: FastEnumMap<EventType, com.badlogic.gdx.utils.Array<EventActionGroup>> = FastEnumMap(EventType::class.java)
	val pendingEvents: com.badlogic.gdx.utils.Array<EventArgs> = com.badlogic.gdx.utils.Array(false, 8)

	fun registerHandler(type: EventType, handler:EventActionGroup)
	{
		handlers.get(type).add(handler)
	}
}