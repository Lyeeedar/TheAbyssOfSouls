package com.lyeeedar.SceneTimeline

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.UnsmoothedPath
import com.lyeeedar.Util.getRotation

class DestinationRenderableAction() : AbstractTimelineAction()
{
	lateinit var renderable: Renderable
	lateinit var slot: SpaceSlot
	var entityPerTile = false
	var killOnEnd = true

	val entities = Array<Entity>()

	override fun enter()
	{
		if (entityPerTile)
		{
			for (tile in parent.destinationTiles)
			{
				val entity = Entity()

				val r = renderable.copy()
				entity.add(RenderableComponent(r))
				entity.add(PositionComponent())
				val pos = entity.pos()!!

				pos.position = tile
				pos.slot = slot

				Global.engine.addEntity(entity)

				entities.add(entity)
			}
		}
		else
		{
			if (parent.destinationTiles.size == 0) return

			val entity = Entity()

			val r = renderable.copy()
			entity.add(RenderableComponent(r))
			entity.add(PositionComponent())
			val pos = entity.pos()!!

			pos.min = parent.destinationTiles.minBy(Tile::hashCode)!!
			pos.max = parent.destinationTiles.maxBy(Tile::hashCode)!!
			pos.slot = slot
			pos.facing = parent.facing

			r.size[0] = (pos.max.x - pos.min.x) + 1
			r.size[1] = (pos.max.y - pos.min.y) + 1

			if (r is ParticleEffect)
			{
				r.rotation = pos.facing.angle
				r.position.set(r.size[0].toFloat() * 0.5f, r.size[1].toFloat() * 0.5f)

				if (pos.facing.x != 0)
				{
					val temp = r.size[0]
					r.size[0] = r.size[1]
					r.size[1] = temp
				}
			}

			Global.engine.addEntity(entity)

			entities.add(entity)
		}
	}

	override fun exit()
	{
		for (entity in entities)
		{
			val renderable = entity.renderable()!!.renderable
			if (renderable is ParticleEffect)
			{
				if (killOnEnd) Global.engine.removeEntity(entity)
				else
				{
					renderable.stop()
				}
			}
			else
			{
				Global.engine.removeEntity(entity)
			}
		}
		entities.clear()
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = DestinationRenderableAction()
		out.parent = parent
		out.renderable = renderable.copy()
		out.slot = slot
		out.startTime = startTime
		out.duration = duration
		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		slot = SpaceSlot.valueOf(xml.get("Slot", "Entity").toUpperCase())
		renderable = AssetManager.loadRenderable(xml.getChildByName("Renderable"))
		entityPerTile = xml.getBoolean("RenderablePerTile", false)
		killOnEnd = xml.getBoolean("KillOnEnd", true)
	}
}

class MovementRenderableAction() : AbstractTimelineAction()
{
	lateinit var renderable: Renderable
	lateinit var slot: SpaceSlot
	var useLeap: Boolean = false

	lateinit var entity: Entity

	override fun enter()
	{
		entity = Entity()

		val r = renderable.copy()
		entity.add(RenderableComponent(r))
		entity.add(PositionComponent())
		val pos = entity.pos()!!

		val min = parent.destinationTiles.minBy(Tile::hashCode)!!
		val max = parent.destinationTiles.maxBy(Tile::hashCode)!!
		val midPoint = min + (max - min) / 2

		pos.position = min.level.getTileClamped(midPoint)
		pos.slot = slot

		r.rotation = getRotation(parent.sourceTile!!, pos.position)

		if (useLeap)
		{
			r.animation = LeapAnimation.obtain().set(duration, pos.position.getPosDiff(parent.sourceTile!!), 2f)
			r.animation = ExpandAnimation.obtain().set(duration, 0.5f, 1.5f, false)
		}
		else
		{
			r.animation = MoveAnimation.obtain().set(duration, UnsmoothedPath(midPoint.getPosDiff(parent.sourceTile!!)), Interpolation.linear)
		}

		Global.engine.addEntity(entity)
	}

	override fun exit()
	{
		Global.engine.removeEntity(entity)
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = MovementRenderableAction()
		out.parent = parent
		out.renderable = renderable.copy()
		out.useLeap = useLeap
		out.slot = slot
		out.startTime = startTime
		out.duration = duration
		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		slot = SpaceSlot.valueOf(xml.get("Slot", "Entity").toUpperCase())
		useLeap = xml.getBoolean("UseLeap")
		renderable = AssetManager.loadRenderable(xml.getChildByName("Renderable"))
	}
}