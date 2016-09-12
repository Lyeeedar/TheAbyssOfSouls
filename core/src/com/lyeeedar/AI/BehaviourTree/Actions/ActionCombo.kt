package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskCombo
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.task
import com.lyeeedar.Direction

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

		val validCombos = Array<Pair>()
		for (c in combo.combos)
		{
			if (c.current.isValid(entity, pos.facing, c))
			{
				validCombos.add(Pair(c, pos.facing))
			}
		}

		if (validCombos.size == 0)
		{
			for (c in combo.combos)
			{
				for (dir in Direction.CardinalValues)
				{
					if (dir == pos.facing) continue // we already checked these
					if (c.current.isValid(entity, dir, c))
					{
						validCombos.add(Pair(c, dir))
						if (dir.cardinalClockwise == pos.facing || dir.cardinalAnticlockwise == pos.facing) validCombos.add(Pair(c, dir))
					}
				}
			}
		}

		if (validCombos.size > 0)
		{
			val chosen = validCombos.random()

			pos.facing = chosen.direction
			combo.currentCombo = chosen.combo
			val nextTask = TaskCombo(chosen.combo)

			val task = entity.task()
			task.tasks.add(nextTask)

			Mappers.directionalSprite.get(entity)?.currentAnim = chosen.combo.current.anim

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

	data class Pair(val combo: ComboTree, val direction: Direction)
}
