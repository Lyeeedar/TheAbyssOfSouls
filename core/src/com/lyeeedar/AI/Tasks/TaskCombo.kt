package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.directionalSprite
import com.lyeeedar.Components.task
import com.lyeeedar.Direction
import com.lyeeedar.Util.Point

class TaskCombo(val combo: ComboTree, val direction: Direction, val target: Point): AbstractTask()
{
	override fun execute(e: Entity)
	{
		// do combo attack
		e.directionalSprite()?.currentAnim = combo.current.anim

		combo.current.activate(e, direction, target)
	}
}
