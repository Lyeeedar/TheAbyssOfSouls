package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.Tasks.TaskInterrupt
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.ElementType
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Animation.SpinAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.random
import ktx.collections.toGdxArray

class ChargeComboStep : ComboStep()
{
	var dist: Int = 1
	var damage: Int = 0
	var knockbackDist: Int = 1

	override fun activate(entity: Entity, direction: Direction, target: Point)
	{
		val pos = entity.pos()!!
		val start = pos.tile!!

		val faceTiles = pos.getEdgeTiles(direction)

		val hitEntities = ObjectSet<Entity>()

		fun flingEntity(e: Entity)
		{
			val etile = e.tile()!!

			e.task().tasks.clear()
			e.task().tasks.add(TaskInterrupt())

			fun isValid(tile: Tile): Boolean
			{
				for (ix in 0..e.pos().size-1)
				{
					for (iy in 0..e.pos().size-1)
					{
						val t = etile.level.getTile(tile, ix, iy) ?: return false
						for (face in faceTiles)
						{
							if (t.x == face.x) return false
							if (t.y == face.y) return false
						}
						if (t.contents.containsKey(e.pos().slot) || t.contents.containsKey(SpaceSlot.WALL)) return false
					}
				}

				return true
			}

			val chosen = etile.level.grid
								 .filter { !it.contents.containsKey(SpaceSlot.WALL) && !it.contents.containsKey(e.pos().slot) }
								 .filter { it.dist(etile) <= knockbackDist }
								 .filter(::isValid)
								 .random() ?: return

			for (ix in 0..e.pos().size-1)
			{
				for (iy in 0..e.pos().size-1)
				{
					val tile = etile.level.getTile(etile, ix, iy)
					tile?.contents?.remove(e.pos().slot)
				}
			}

			e.pos().position = chosen

			for (ix in 0..e.pos().size-1)
			{
				for (iy in 0..e.pos().size-1)
				{
					val tile = etile.level.getTile(chosen, ix, iy) ?: continue
					tile.contents.put(pos.slot, e)
				}
			}

			e.renderable().renderable.animation = LeapAnimation.obtain().setRelative(0.3f, start, chosen, 2f)
			e.renderable().renderable.animation = SpinAnimation.obtain().set(0.3f, 360f)

			if (e.renderable().renderable is Sprite)
			{
				(e.renderable().renderable as Sprite).removeAmount = 0f
			}
		}

		var prev = pos.tile!!
		var current = pos.tile!!
		outer@for (i in 0..dist)
		{
			for (x in 0..pos.size-1)
			{
				for (y in 0..pos.size-1)
				{
					val t = current.level.getTile(current, x, y) ?: break@outer
					if (t.contents.containsKey(SpaceSlot.WALL)) break@outer

					val e = t.contents[entity.pos().slot]
					if (e != null && e != entity && !hitEntities.contains(e))
					{
						hitEntities.add(e)

						if (damage > 0)
						{
							e.stats().dealDamage(damage, ElementType.NONE, 0f)

							val sprite = entity.renderable()?.renderable as? Sprite ?: continue
							sprite.colourAnimation = BlinkAnimation.obtain().set(Colour(1f, 0.5f, 0.5f, 1f), sprite.colour, 0.15f, true)
						}

						if (e.stats().blocking || e.stats().invulnerable)
						{

						}
						else if (!e.pos().moveable || e.pos().size > knockbackDist)
						{
							break@outer
						}
						else
						{
							flingEntity(e)
						}
					}
				}
			}

			prev = current
			current = current.level.getTile(current, direction) ?: break
		}

		for (x in 0..pos.size-1)
		{
			for (y in 0..pos.size-1)
			{
				val tile = current.level.getTile(start, x, y)
				tile?.contents?.remove(pos.slot)
			}
		}

		pos.position = prev

		for (x in 0..pos.size-1)
		{
			for (y in 0..pos.size-1)
			{
				val tile = prev.level.getTile(prev, x, y) ?: continue

				if (tile.contents.containsKey(pos.slot))
				{
					flingEntity(tile.contents[pos.slot])
				}

				tile.contents.put(pos.slot, entity)
			}
		}

		entity.renderable().renderable.animation = MoveAnimation.obtain().set(prev, start, 0.15f)
	}

	override fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	{
		val hitPoints = ObjectSet<Point>()

		val pos = entity.pos()!!
		var current = pos.tile!!
		outer@for (i in 0..dist + 5)
		{
			for (x in 0..pos.size-1)
			{
				for (y in 0..pos.size-1)
				{
					val t = current.level.getTile(current, x, y) ?: break@outer
					if (t.contents.containsKey(SpaceSlot.WALL)) break@outer
					val contents = t.contents[entity.pos().slot]
					if (contents != null && contents != entity && (!contents.pos().moveable || contents.pos().size > knockbackDist)) break@outer

					hitPoints.add(t)
				}
			}

			current = current.level.getTile(current, direction) ?: break
		}

		return hitPoints.toGdxArray()
	}

	override fun isValid(entity: Entity, direction: Direction, target: Point, tree: ComboTree): Boolean
	{
		val hitTiles = getAllValid(entity, direction)

		return hitTiles.contains(target)
	}

	override fun parse(xml: XmlReader.Element)
	{
		dist = xml.getInt("Distance")
		damage = xml.getInt("Damage", 0)
		knockbackDist = xml.getInt("Knockback", 1)
	}
}