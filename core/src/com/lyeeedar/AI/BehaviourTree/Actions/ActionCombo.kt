package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskCombo
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Components.combo
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.task
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.weightedRandom

class ActionCombo : AbstractAction()
{
	lateinit var key: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val target = getData<Point>( key, null )

		val combo = entity.combo()
		val pos = entity.pos()

		val tile = pos.position as? Tile

		state = ExecutionState.FAILED
		if (target == null || combo == null || pos == null || tile == null) return state

		if (combo.currentCombo != null)
		{
			// continue comboStep
			val current = combo.currentCombo!!.comboStep

			val validTargets = Array<Direction>()
			if (current.isValid(entity, pos.facing, target, combo.currentCombo!!)) validTargets.add(pos.facing)

			if (current.canTurn && validTargets.size == 0)
			{
				if (current.isValid(entity, pos.facing.cardinalClockwise, target, combo.currentCombo!!)) validTargets.add(pos.facing.cardinalClockwise)
				if (current.isValid(entity, pos.facing.cardinalAnticlockwise, target, combo.currentCombo!!)) validTargets.add(pos.facing.cardinalAnticlockwise)
			}

			if (validTargets.size != 0)
			{
				val dir = validTargets.random()
				val nextTask = TaskCombo(combo.currentCombo!!, dir, target)

				val task = entity.task()
				task.tasks.add(nextTask)

				pos.facing = dir
			}
			else
			{
				val ranTarget = current.getAllValid(entity, pos.facing).random()

				val task = entity.task()
				if (ranTarget == null)
				{
					task.tasks.add(TaskWait())
				}
				else
				{
					val nextTask = TaskCombo(combo.currentCombo!!, pos.facing, ranTarget)

					task.tasks.add(nextTask)
				}
			}

			combo.currentCombo = combo.currentCombo!!.random.asSequence().weightedRandom(fun (c) = c.chooseChance)

			state = if (combo.currentCombo != null) ExecutionState.RUNNING else ExecutionState.COMPLETED
		}
		else
		{
			// start new
			val validCombos = Array<Pair>()
			for (c in combo.combos.random)
			{
				if (c.cooldownTimer == 0 && c.comboStep.isValid(entity, pos.facing, target, c))
				{
					validCombos.add(Pair(c, pos.facing))
				}
			}

			if (validCombos.size == 0)
			{
				for (c in combo.combos.random)
				{
					for (dir in Direction.CardinalValues)
					{
						if (dir == pos.facing) continue // we already checked these
						if (c.cooldownTimer == 0 && c.comboStep.isValid(entity, dir, target, c))
						{
							validCombos.add(Pair(c, dir))
							if (dir.cardinalClockwise == pos.facing || dir.cardinalAnticlockwise == pos.facing) validCombos.add(Pair(c, dir))
						}
					}
				}
			}

			if (validCombos.size > 0)
			{
				val chosen = validCombos.asSequence().weightedRandom(fun (c) = c.combo.chooseChance)

				if (chosen != null)
				{
					pos.facing = chosen.direction
					combo.currentCombo = chosen.combo
					val nextTask = TaskCombo(chosen.combo, pos.facing, target)

					val task = entity.task()
					task.tasks.add(nextTask)

					combo.currentCombo = combo.currentCombo!!.random.asSequence().weightedRandom(fun(c) = c.chooseChance)

					state = ExecutionState.RUNNING
				}
			}
		}

		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.getAttribute("Key").toLowerCase()
	}

	override fun cancel(entity: Entity)
	{

	}

	data class Pair(val combo: ComboTree, val direction: Direction)
}
