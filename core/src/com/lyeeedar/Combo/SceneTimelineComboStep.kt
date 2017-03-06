package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.random
import com.lyeeedar.Util.toHitPointArray
import ktx.collections.toGdxArray


class SceneTimelineComboStep : ComboStep()
{
	enum class HitType
	{
		ALL,
		TARGET,
		RANDOM
	}

	val hitPoints = com.badlogic.gdx.utils.Array<Point>()
	lateinit var hitType: HitType
	var hitCount = 1
	lateinit var sceneTimeline: SceneTimeline
	var stepForward = false

	fun canMove(rootEntity: Entity, entity: Entity, direction: Direction, toMove: Array<Entity>? = null): Boolean
	{
		val entityTile = rootEntity.tile() ?: return true
		val parentPos = rootEntity.pos() ?: return true

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
						if (!epos.moveable || epos.size > parentPos.size)
						{
							return false
						}
						else
						{
							val canmove = canMove(rootEntity, e, direction, toMove)
							if (canmove)
							{
								if (toMove != null && !toMove.contains(e, true))
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

	fun doMove(rootEntity: Entity, entity: Entity, direction: Direction)
	{
		val parentPos = rootEntity.pos() ?: return

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
		val oldfacing = pos.facing
		pos.position = next
		pos.facing = oldfacing

		for (x in 0..pos.size-1)
		{
			for (y in 0..pos.size-1)
			{
				val tile = next.level.getTile(next, x, y)
				tile?.contents?.put(pos.slot, entity)
			}
		}

		val renderable = entity.renderable() ?: return
		val anim = MoveAnimation.obtain().set(next, prev, 0.15f)
		renderable.renderable.animation = anim

		var xDiff = pos.x - parentPos.x
		if (xDiff > 0) xDiff -= parentPos.size - 1

		var yDiff = pos.y - parentPos.y
		if (yDiff > 0) yDiff -= parentPos.size - 1

		val diff = if (xDiff != 0) xDiff else yDiff

		anim.startDelay = 0.03f * Math.abs(diff)
	}

	override fun activate(entity: Entity, direction: Direction, target: Point)
	{
		if (stepForward)
		{
			val toMove = Array<Entity>()
			if (canMove(entity, entity, direction, toMove))
			{
				for (e in toMove)
				{
					doMove(entity, e, direction)
				}

				doMove(entity, entity, direction)
			}
		}

		val source = entity.pos()!!.getEdgeTiles(direction).random()
		val hitTiles = when (hitType)
		{
			HitType.ALL -> getAllValid(entity, direction).map { it as Tile }.toGdxArray()
			HitType.TARGET -> arrayOf(source.level.getTileClamped(target)).toGdxArray()
			HitType.RANDOM -> getAllValid(entity, direction).map { it as Tile }.asSequence().random(hitCount).asIterable().toGdxArray()
			else -> throw Exception("Unhandled hittype '$hitType'!")
		}

		val timeline = sceneTimeline.copy()
		timeline.sourceTile = source
		timeline.destinationTiles.addAll(hitTiles)
		timeline.facing = direction

		for (tile in hitTiles)
		{
			tile.timelines.add(timeline)
		}

		val timelineEntity = Entity()
		val component = SceneTimelineComponent(timeline)
		timelineEntity.add(component)

		Global.engine.addEntity(timelineEntity)
	}

	override fun isValid(entity: Entity, direction: Direction, target: Point, tree: ComboTree): Boolean
	{
		var hitTiles = getAllValid(entity, direction)

		if (hitTiles.contains(target)) return true

		if (stepForward && canMove(entity, entity, direction))
		{
			hitTiles = hitTiles.map { (it as Tile).level.getTile(it, direction) }.filterNotNull().toGdxArray()

			return hitTiles.contains(target)
		}

		return false
	}

	override fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	{
		val epos = entity.pos()!!
		val etile = entity.tile()!!
		val tiles = com.badlogic.gdx.utils.Array<Point>(false, 16)

		var xstep = 0
		var ystep = 0

		var sx = 0
		var sy = 0

		if (direction === Direction.NORTH)
		{
			sx = 0
			sy = epos.size - 1

			xstep = 1
			ystep = 0
		}
		else if (direction === Direction.SOUTH)
		{
			sx = 0
			sy = 0

			xstep = 1
			ystep = 0
		}
		else if (direction === Direction.EAST)
		{
			sx = epos.size - 1
			sy = 0

			xstep = 0
			ystep = 1
		}
		else if (direction === Direction.WEST)
		{
			sx = 0
			sy = 0

			xstep = 0
			ystep = 1
		}

		for (i in 0..epos.size - 1)
		{
			val attackerTile = etile.level.getTile(etile, sx + xstep * i, sy + ystep * i)!!

			val mat = Matrix3()
			mat.setToRotation(direction.angle)
			val vec = Vector3()

			for (point in hitPoints)
			{
				vec.set(point.x.toFloat(), point.y.toFloat(), 0f)
				vec.mul(mat)

				val dx = Math.round(vec.x)
				val dy = Math.round(vec.y)

				val tile = attackerTile.level.getTile(attackerTile, dx, dy)
				if (tile != null) tiles.add(tile)
			}
		}

		// restrict by visibility and remove duplicates
		val visibleTiles = entity.shadow().cache.currentShadowCastSet
		val existingSet = ObjectSet<Point>()

		val itr = tiles.iterator()
		while (itr.hasNext())
		{
			val pos = itr.next()

			// Remove not visible and duplicates
			if (visibleTiles.contains(pos))
			{
				existingSet.add(pos)
			}
		}

		return existingSet.toGdxArray(false)
	}

	override fun parse(xml: XmlReader.Element)
	{
		sceneTimeline = SceneTimeline.load(xml.getChildByName("SceneTimeline"))
		hitType = HitType.valueOf(xml.get("HitType", "All").toUpperCase())
		hitCount = xml.getInt("HitCount", 1)
		stepForward = xml.getBoolean("StepForward", false)

		val hitPointsEl = xml.getChildByName("HitPattern")
		hitPoints.addAll(hitPointsEl.toHitPointArray())
	}
}
