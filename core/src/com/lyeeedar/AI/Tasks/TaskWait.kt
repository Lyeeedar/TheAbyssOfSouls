package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.directionalSprite
import com.lyeeedar.Components.stats

/**
 * Created by Philip on 22-Mar-16.
 */

class TaskWait(): AbstractTask()
{
	override fun execute(e: Entity)
	{
		e.directionalSprite()?.currentAnim = "wait"

		if (e.stats() != null) e.stats().stamina += 4
	}
}