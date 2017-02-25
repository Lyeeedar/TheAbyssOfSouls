package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.combo

class TaskInterrupt : AbstractTask()
{
	override fun execute(e: Entity)
	{
		e.combo()?.currentCombo = null
	}
}