package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.task

class TaskCombo(val step: ComboStep): AbstractTask(EventComponent.EventType.ATTACK)
{
	override fun execute(e: Entity)
	{
		// do combo attack
		val next = step.activate(e)

		if (next != null)
		{
			val nextTask = TaskCombo(next)

			val task = e.task()

			task.tasks.add(nextTask)

			Mappers.directionalSprite.get(e)?.currentAnim = next.anim
		}
		else
		{
			Mappers.directionalSprite.get(e)?.currentAnim = "idle"
		}

		val combo = e.combo()
		combo.currentCombo = next
	}
}
