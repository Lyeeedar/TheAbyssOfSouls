package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskCombo
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.task

class ActionCombo : AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		val combo = entity.combo()
		val pos = entity.pos()

		state = ExecutionState.FAILED
		if (combo == null || pos == null) return state

		if (combo.currentCombo != null)
		{
			throw RuntimeException("Managed to run ai whilst a combo was in process!")
		}

		val validCombos = Array<ComboStep>()
		for (c in combo.combos)
		{
			if (c.isValid(entity, pos.facing))
			{
				validCombos.add(c)
			}
		}

		if (validCombos.size > 0)
		{
			val chosen = validCombos.random()

			combo.currentCombo = chosen
			val nextTask = TaskCombo(chosen)

			val task = entity.task()
			task.tasks.add(nextTask)

			Mappers.directionalSprite.get(entity)?.currentAnim = chosen.anim

			state = ExecutionState.COMPLETED
		}

		return state
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel()
	{
		// cant do anything here
	}
}
