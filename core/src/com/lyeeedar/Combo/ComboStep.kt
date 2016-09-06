package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.pos
import com.lyeeedar.Direction

abstract class ComboStep
{
	var anim: String = "attack"
	var canTurn: Boolean = false
	var canStop: Boolean = false
	var stepsForward: Int = 0

	fun activate(entity: Entity): ComboStep?
	{
		val pos = entity.pos() ?: return null

		val validTargets = Array<Direction>()

		if (isValid(entity, pos.facing)) validTargets.add(pos.facing)

		if (canTurn && validTargets.size == 0)
		{
			if (isValid(entity, pos.facing.cardinalClockwise)) validTargets.add(pos.facing.cardinalClockwise)
			if (isValid(entity, pos.facing.cardinalAnticlockwise)) validTargets.add(pos.facing.cardinalAnticlockwise)
		}

		if (canStop && validTargets.size == 0)
		{
			return null
		}

		val dir = validTargets.random() ?: pos.facing

		pos.facing = dir

		doActivate(entity, dir)

		return nextSteps.random()
	}

	abstract fun doActivate(entity: Entity, direction: Direction)
	abstract fun isValid(entity: Entity, direction: Direction): Boolean
	abstract fun parse(xml: XmlReader.Element)

	val nextSteps = Array<ComboStep>()

	companion object
	{
		fun load(xml: XmlReader.Element): ComboStep
		{
			val step: ComboStep = when (xml.name.toUpperCase())
			{
				"WAIT" -> WaitComboStep()
				"SLASH" -> SlashComboStep()
				else -> throw NotImplementedError("Unknown combo step type: " + xml.name.toUpperCase())
			}

			step.anim = xml.get("Anim", "attack")
			step.canStop = xml.getBoolean("CanStop", false)
			step.canTurn = xml.getBoolean("CanTurn", false)
			step.stepsForward = xml.getInt("Steps", 0)

			step.parse(xml)

			return step
		}
	}
}