package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Util.Point

class TaskCombo(val combo: ComboTree, val direction: Direction, val target: Point): AbstractTask()
{
	override fun execute(e: Entity)
	{
		e.stats().stamina -= combo.cost

		// do combo attack
		e.directionalSprite()?.currentAnim = combo.comboStep.anim

		combo.comboStep.activate(e, direction, target)
	}
}
