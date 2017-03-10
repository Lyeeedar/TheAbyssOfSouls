package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.directionalSprite
import com.lyeeedar.Components.stats
import com.lyeeedar.Components.task
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Systems.task
import com.lyeeedar.Util.Point

class TaskCombo(val combo: ComboTree, val direction: Direction, val target: Point): AbstractTask()
{
	override fun execute(e: Entity)
	{
		e.stats().stamina -= combo.staminaCost

		if (combo.turnCost > 1)
		{
			for (i in 1..combo.turnCost - 1)
			{
				e.task().tasks.add(TaskWait(false))
			}
		}

		// do combo attack
		e.directionalSprite()?.currentAnim = combo.comboStep.anim

		combo.comboStep.activate(e, direction, target, combo)

		if (combo.cooldown > 0)
		{
			combo.cooldownTimer = combo.cooldown
			Global.engine.task().onTurnEvent += fun (): Boolean
			{
				combo.cooldownTimer--

				return combo.cooldownTimer == 0
			}
		}
	}
}
