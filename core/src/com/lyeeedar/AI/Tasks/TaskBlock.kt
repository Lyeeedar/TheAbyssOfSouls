package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.event
import com.lyeeedar.Components.stats

class TaskBlock : AbstractTask()
{
	lateinit var func: () -> Unit

	override fun execute(e: Entity)
	{
		func = fun ()
		{
			e.stats().blocking = false
			e.event().onTurn -= func
		}

		e.stats().blocking = true
		e.event().onTurn += func
	}
}
