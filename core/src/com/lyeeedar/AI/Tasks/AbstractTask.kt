package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.EventComponent

/**
 * Created by Philip on 22-Mar-16.
 */

abstract class AbstractTask(val eventType: EventComponent.EventType)
{
	var duration: Float = 1f

	abstract fun execute(e: Entity)
}