package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
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
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point

class SlashComboStep : ComboStep()
{
	var range: Int = 1
	var effect: ParticleEffect = AssetManager.loadParticleEffect("slash")

	override fun doActivate(entity: Entity, direction: Direction)
	{
		val pos = entity.pos() ?: return
		val entityTile = entity.tile() ?: return

		val (min, max) = getMinMax(entity, direction)
		max.x += direction.x * range
		max.y += direction.y * range

		val hitSet = ObjectSet<Tile>()

		val e = effect.copy()
		e.sizex = pos.size + 2f
		e.sizey = range.toFloat()
		e.rotation = direction.angle
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

		val center = min + ((max-min) / 2)
		val ctile = entityTile.level.getTile(center) ?: return

		val slashEntity = Entity()
		slashEntity.add(PositionComponent(ctile, SpaceSlot.AIR))
		slashEntity.add(ParticleComponent(e))

		Global.engine.addEntity(slashEntity)
	}

	override fun isValid(entity: Entity, direction: Direction): Boolean
	{
		val entityTile = entity.tile() ?: return false
		val entityStats = entity.stats() ?: return false

		// get min and max
		val (min, max) = getMinMax(entity, direction)

		// check total range
		val totalRange = range + stepsForward

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
			min = pos.position + Point(-1, pos.size + 1)
			max = min + Point(pos.size + 1, 0)
		}
		else if (direction == Direction.WEST)
		{
			min = pos.position + Point.MINUS_ONE
			max = min + Point(0, pos.size + 1)
		}
		else if (direction == Direction.EAST)
		{
			min = pos.position + Point(pos.size + 1, -1)
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
		effect = AssetManager.loadParticleEffect(xml.getChildByName("Particle"))
	}
}

data class MinMax(val min: Point, val max: Point)
