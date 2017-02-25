package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.event
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.stats
import com.lyeeedar.Direction
import com.lyeeedar.Util.Point

class DefenseComboStep : ComboStep()
{
	enum class DefenseMode
	{
		BLOCK,
		INVULNERABLE
	}

	enum class MoveType
	{
		ROLL,
		LEAP
	}

	enum class MoveDirection
	{
		SAME,
		OPPOSITE
	}

	lateinit var mode: DefenseMode
	var moveDist = 0
	lateinit var moveType: MoveType
	lateinit var moveDirection: MoveDirection

	override fun activate(entity: Entity, direction: Direction, target: Point)
	{
		if (mode == DefenseMode.BLOCK)
		{
			entity.stats().blocking = true

			entity.event().onTurn += fun(): Boolean {
				entity.stats().blocking = false
				return true
			}
		}
		else if (mode == DefenseMode.INVULNERABLE)
		{
			entity.stats().invulnerable = true

			entity.event().onTurn += fun(): Boolean {
				entity.stats().invulnerable = false
				return true
			}
		}
		else throw UnsupportedOperationException()
	}

	override fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	{
		val out = Array<Point>()
		out.add(entity.pos().position)
		return out
	}

	override fun isValid(entity: Entity, direction: Direction, target: Point, tree: ComboTree): Boolean
	{
		for (child in tree.random)
		{
			if (child.comboStep.isValid(entity, direction, target, child)) return true
		}

		return tree.random.size == 0
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = DefenseMode.valueOf(xml.get("Type", "Block").toUpperCase())
		moveDist = xml.getInt("Move", 0)
		moveType = MoveType.valueOf(xml.get("Animation", "Roll").toUpperCase())
		moveDirection = MoveDirection.valueOf(xml.get("Direction", "Same").toUpperCase())
	}
}
