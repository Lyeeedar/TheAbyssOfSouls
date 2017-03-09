package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Animation.SpinAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
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

	override fun activate(entity: Entity, direction: Direction, target: Point, tree: ComboTree)
	{
		val origDirection = direction

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

		if (moveDist > 0)
		{
			val direction = if (this.moveDirection == MoveDirection.SAME) direction else direction.opposite
			val start = entity.tile()!!
			var end = start

			outer@for (i in 1..moveDist)
			{
				for (x in 0..entity.pos().size-1)
				{
					for (y in 0..entity.pos().size-1)
					{
						val etile = start.level.getTile(start, direction.x * i + x, direction.y * i + y) ?: break@outer
						if (etile.contents.containsKey(SpaceSlot.WALL)) break@outer
						if (etile.contents.containsKey(entity.pos().slot) && etile.contents[entity.pos().slot] != entity) break@outer
					}
				}

				end = start.level.getTile(start, direction.x * i, direction.y * i)!!
			}

			for (ix in 0..entity.pos().size-1)
			{
				for (iy in 0..entity.pos().size-1)
				{
					val tile = start.level.getTile(start, ix, iy)
					tile?.contents?.remove(entity.pos().slot)
				}
			}

			entity.pos().position = end
			entity.pos().facing = origDirection

			for (ix in 0..entity.pos().size-1)
			{
				for (iy in 0..entity.pos().size-1)
				{
					val tile = end.level.getTile(end, ix, iy) ?: continue
					tile.contents.put(entity.pos().slot, entity)
				}
			}

			if (moveType == MoveType.ROLL)
			{
				entity.renderable().renderable.animation = MoveAnimation.obtain().set(end, start, 0.3f)

				if (direction == Direction.EAST)
				{
					entity.renderable().renderable.animation = SpinAnimation.obtain().set(0.3f, -360f)
				}
				else
				{
					entity.renderable().renderable.animation = SpinAnimation.obtain().set(0.3f, 360f)
				}

				entity.renderable().renderable.animation = ExpandAnimation.obtain().set(0.2f, 1f, 0.8f, false)

				if (entity.renderable().renderable is Sprite)
				{
					(entity.renderable().renderable as Sprite).removeAmount = 0f
				}
			}
			else if (moveType == MoveType.LEAP)
			{
				entity.renderable().renderable.animation = LeapAnimation.obtain().setRelative(0.3f, start, end, 2f)

				if (entity.renderable().renderable is Sprite)
				{
					(entity.renderable().renderable as Sprite).removeAmount = 0f
				}
			}
			else throw UnsupportedOperationException()
		}
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
		moveType = MoveType.valueOf(xml.get("AnimType", "Roll").toUpperCase())
		moveDirection = MoveDirection.valueOf(xml.get("Direction", "Same").toUpperCase())
	}
}
