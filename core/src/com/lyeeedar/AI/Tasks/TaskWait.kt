package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.EventComponent

/**
 * Created by Philip on 22-Mar-16.
 */

class TaskWait(): AbstractTask(EventComponent.EventType.WAIT)
{
	override fun execute(e: Entity)
	{

	}
}