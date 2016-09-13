package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.pos
import com.lyeeedar.Direction
import com.lyeeedar.Util.set

abstract class ComboStep
{
	var anim: String = "attack"
	var canTurn: Boolean = false
	var canStop: Boolean = false

	fun activate(entity: Entity, comboTree: ComboTree): Boolean
	{
		val pos = entity.pos() ?: return false

		val validTargets = Array<Direction>()

		if (isValid(entity, pos.facing, comboTree)) validTargets.add(pos.facing)

		if (canTurn && validTargets.size == 0)
		{
			if (isValid(entity, pos.facing.cardinalClockwise, comboTree)) validTargets.add(pos.facing.cardinalClockwise)
			if (isValid(entity, pos.facing.cardinalAnticlockwise, comboTree)) validTargets.add(pos.facing.cardinalAnticlockwise)
		}

		if (canStop && validTargets.size == 0)
		{
			return false
		}

		val dir = validTargets.random() ?: pos.facing

		pos.facing = dir

		doActivate(entity, dir)

		return true
	}

	abstract fun doActivate(entity: Entity, direction: Direction)
	abstract fun isValid(entity: Entity, direction: Direction, comboTree: ComboTree): Boolean
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): ComboStep
		{
			val step: ComboStep = when (xml.get("Type").toUpperCase())
			{
				"WAIT" -> WaitComboStep()
				"SLASH" -> SlashComboStep()
				else -> throw NotImplementedError("Unknown combo step type: " + xml.name.toUpperCase())
			}

			step.anim = xml.get("Anim", "attack")
			step.canStop = xml.getBoolean("CanStop", false)
			step.canTurn = xml.getBoolean("CanTurn", false)

			step.parse(xml)

			return step
		}
	}
}

class ComboTree
{
	lateinit var current: ComboStep
	val next = Array<ComboTree>()

	fun activate(entity: Entity): ComboTree?
	{
		val advance = current.activate(entity, this)

		return if (advance) next.random() else null
	}

	companion object
	{
		fun load(xml: XmlReader.Element): Array<ComboTree>
		{
			val map = ObjectMap<String, ComboStep>()

			val steps = xml.getChildByName("Steps")
			for (i in 0..steps.childCount-1)
			{
				val el = steps.getChild(i)
				val key = el.name.toLowerCase()
				val step = ComboStep.load(el)

				map[key] = step
			}

			fun recursiveParse(xml: XmlReader.Element): ComboTree
			{
				val comboTree = ComboTree()

				val key = xml.name.toLowerCase()
				comboTree.current = map[key]

				for (i in 0..xml.childCount-1)
				{
					val el = xml.getChild(i)
					val child = recursiveParse(el)
					comboTree.next.add(child)
				}

				return comboTree
			}

			val trees = Array<ComboTree>()

			val tree = xml.getChildByName("Tree")
			for (i in 0..tree.childCount-1)
			{
				val el = tree.getChild(i)
				val ctree = recursiveParse(el)

				trees.add(ctree)
			}

			return trees
		}
	}
}