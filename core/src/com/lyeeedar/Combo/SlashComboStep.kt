package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.ElementType
import com.lyeeedar.Events.EventActionDamage
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Pathfinding.BresenhamLine
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point

class SlashComboStep : ComboStep()
{
	var range: Int = 1
	lateinit var effect: ParticleEffect
	var stepForward: Boolean = true

	override fun doActivate(entity: Entity, direction: Direction)
	{
		val entityTile = entity.tile() ?: return

		var delay = 0f

		// if step forward then move forward
		if (stepForward)
		{
			fun canMove(entity: Entity, toMove: Array<Entity>): Boolean
			{
				val pos = entity.pos() ?: return true
				for (x in 0..pos.size-1)
				{
					for (y in 0..pos.size-1)
					{
						val tile = entityTile.level.getTile(pos.position, x + direction.x, y + direction.y)

						if (tile == null)
						{
							return false
						}
						else if (tile.contents[SpaceSlot.WALL] != null)
						{
							return false
						}
						else
						{
							val e = tile.contents[pos.slot]
							if (e != null && e != entity)
							{
								val epos = e.pos() ?: continue
								if (epos.size > pos.size)
								{
									return false
								}
								else
								{
									val canmove = canMove(e, toMove)
									if (canmove)
									{
										if (!toMove.contains(e, true))
										{
											toMove.add(e)
										}
									}
									else
									{
										return false
									}
								}
							}
						}
					}
				}

				return true
			}

			val toMove = Array<Entity>()
			val canmove = canMove(entity, toMove)

			if (canmove)
			{
				fun doMove(entity: Entity)
				{
					val pos = entity.pos() ?: return
					val prev = entity.tile() ?: return

					for (x in 0..pos.size-1)
					{
						for (y in 0..pos.size-1)
						{
							val tile = prev.level.getTile(prev, x, y)

							if (tile?.contents?.get(pos.slot) == entity) tile?.contents?.remove(pos.slot)
						}
					}

					val next = prev.level.getTile(prev, direction) ?: return
					pos.position = next

					for (x in 0..pos.size-1)
					{
						for (y in 0..pos.size-1)
						{
							val tile = next.level.getTile(next, x, y)
							tile?.contents?.put(pos.slot, entity)
						}
					}

					val sprite = Mappers.sprite.get(entity)
					sprite.sprite.animation = MoveAnimation.obtain().set(next, prev, 0.15f)
				}

				for (e in toMove)
				{
					doMove(e)
				}

				doMove(entity)
				delay = 0.1f
			}
		}

		val (min, max) = getMinMax(entity, direction)

		if (min.x > max.x)
		{
			val temp = min.x
			min.x = max.x
			max.x = temp
		}

		if (min.y > max.y)
		{
			val temp = min.y
			min.y = max.y
			max.y = temp
		}

		if (direction.x < 0 || direction.y < 0)
		{
			min.x += direction.x * (range - 1)
			min.y += direction.y * (range - 1)
		}
		else
		{
			max.x += direction.x * (range - 1)
			max.y += direction.y * (range - 1)
		}

		val hitSet = ObjectSet<Tile>()

		val e = effect.copy()
		e.collisionFun = fun (cx: Int, cy: Int) {
			val t = entityTile.level.getTile(cx, cy) ?: return
			if (!hitSet.contains(t))
			{
				hitSet.add(t)

				if (t.liesInRect(min, max))
				{
					val eventEntity = Entity()
					val event = EventComponent()
					eventEntity.add(event)

					val group = EventActionGroup()
					group.actions.add(EventActionDamage(group, ElementType.getElementMap(1f)))
					event.registerHandler(EventComponent.EventType.HIT, group)

					eventEntity.add(PositionComponent(t))

					Global.engine.addEntity(eventEntity)
				}
			}
		}
		e.renderDelay = delay

		val epos = PositionComponent()
		epos.slot = SpaceSlot.AIR
		epos.min = entityTile.level.getTileClamped(min)!!
		epos.max = entityTile.level.getTileClamped(max)!!
		epos.facing = direction

		val slashEntity = Entity()
		slashEntity.add(epos)
		slashEntity.add(ParticleComponent(e))

		Global.engine.addEntity(slashEntity)
	}

	override fun isValid(entity: Entity, direction: Direction, comboTree: ComboTree): Boolean
	{
		val entityTile = entity.tile() ?: return false
		val entityStats = entity.stats() ?: return false

		// get min and max
		val (min, max) = getMinMax(entity, direction)

		// check total range
		val totalRange = (range-1) + if (stepForward) 1 else 0

		for (i in 0..totalRange)
		{
			for (p in min..max)
			{
				val point = p + Point(direction.x * i, direction.y * i)

				val t = entityTile.level.getTile(point) ?: continue

				for (e in t.contents)
				{
					val stats = e.stats() ?: continue
					if (!entityStats.factions.isAllies(stats.factions))
					{
						// check that entity is visible
						val path = BresenhamLine.lineNoDiag(entityTile.x, entityTile.y, t.x, t.y, entityTile.level.grid.array, true, Integer.MAX_VALUE, SpaceSlot.AIR, entity)
						if (path.last() == t)
						{
							return true
						}
					}
				}
			}
		}

		return false
	}

	fun getMinMax(entity: Entity, direction: Direction): MinMax
	{
		val pos = entity.pos() ?: return MinMax(Point.MINUS_ONE, Point.MINUS_ONE)

		// get min and max
		val min: Point
		val max: Point

		if (direction == Direction.SOUTH)
		{
			min = pos.position + Point.MINUS_ONE
			max = min + Point(pos.size + 1, 0)
		}
		else if (direction == Direction.NORTH)
		{
			min = pos.position + Point(-1, pos.size)
			max = min + Point(pos.size + 1, 0)
		}
		else if (direction == Direction.WEST)
		{
			min = pos.position + Point.MINUS_ONE
			max = min + Point(0, pos.size + 1)
		}
		else if (direction == Direction.EAST)
		{
			min = pos.position + Point(pos.size, -1)
			max = min + Point(0, pos.size + 1)
		}
		else
		{
			return MinMax(Point.MINUS_ONE, Point.MINUS_ONE)
		}

		return MinMax(min, max)
	}

	override fun parse(xml: XmlReader.Element)
	{
		range = xml.getInt("Range", 1)
		stepForward = xml.getBoolean("StepForward", true)
		effect = AssetManager.loadParticleEffect(xml.getChildByName("Effect"))
	}
}

data class MinMax(val min: Point, val max: Point)
