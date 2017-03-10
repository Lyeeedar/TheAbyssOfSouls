package com.lyeeedar.SceneTimeline

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.*
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.render
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.UnsmoothedPath
import com.lyeeedar.Util.getRotation

class SourceRenderableAction() : AbstractTimelineAction()
{
	lateinit var renderable: Renderable

	var restoreOriginal = true
	var originalRenderable: Renderable? = null

	override fun enter()
	{
		val source = parent.parentEntity ?: parent.sourceTile!!.contents.firstOrNull{ it.sceneTimeline()?.sceneTimeline == parent } ?: return

		if (source.renderable() == null)
		{
			source.add(RenderableComponent())
		}
		else
		{
			originalRenderable = source.renderable().renderable
		}

		source.renderable().renderable = renderable.copy()
	}

	override fun exit()
	{
		val source = parent.parentEntity ?: parent.sourceTile!!.contents.firstOrNull{ it.sceneTimeline()?.sceneTimeline == parent } ?: return

		if (originalRenderable != null)
		{
			source.renderable().renderable = originalRenderable!!
		}
		else
		{
			source.remove(RenderableComponent::class.java)
		}

		originalRenderable = null
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = SourceRenderableAction()
		action.parent = parent

		action.startTime = startTime
		action.duration = duration

		action.renderable = renderable
		action.restoreOriginal = restoreOriginal

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		renderable = AssetManager.loadRenderable(xml.getChildByName("Renderable"))
		restoreOriginal = xml.getBoolean("RestoreOriginal", true)
	}
}

class SourceAnimationAction() : AbstractTimelineAction()
{
	enum class Animation
	{
		EXPAND,
		SPIN,
		FADE
	}

	lateinit var anim: Animation

	var startSize = 1f
	var endSize = 1f
	var oneWay = true

	var startFade = 1f
	var endFade = 1f

	var spinAngle = 0f

	override fun enter()
	{
		val source = parent.parentEntity ?: parent.sourceTile!!.contents.firstOrNull{ it.sceneTimeline()?.sceneTimeline == parent } ?: return
		val sourceRenderable = source.renderable()?.renderable ?: return

		if (anim == Animation.EXPAND)
		{
			sourceRenderable.animation = ExpandAnimation.obtain().set(duration, startSize, endSize, oneWay)
		}
		else if (anim == Animation.SPIN)
		{
			sourceRenderable.animation = SpinAnimation.obtain().set(duration, spinAngle)
		}
		else if (anim == Animation.FADE)
		{
			sourceRenderable.animation = AlphaAnimation.obtain().set(duration, startFade, endFade)
		}
		else
		{
			throw Exception("Unhandled animation type '$anim'!")
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = SourceAnimationAction()
		action.parent = parent

		action.startTime = startTime
		action.duration = duration

		action.anim = anim
		action.startSize = startSize
		action.endSize = endSize
		action.oneWay = oneWay
		action.spinAngle = spinAngle
		action.startFade = startFade
		action.endFade = endFade

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		anim = Animation.valueOf(xml.get("Animation", "Expand").toUpperCase())
		startSize = xml.getFloat("SizeStart", 1f)
		endSize = xml.getFloat("SizeEnd", 1f)
		oneWay = xml.getBoolean("OneWay", false)
		spinAngle = xml.getFloat("Angle", 0f)
		startFade = xml.getFloat("FadeStart", 1f)
		endFade = xml.getFloat("FadeEnd", 1f)
	}

}


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

			if (r is ParticleEffect && r.useFacing)
			{
				r.rotation = pos.facing.angle
				r.facing = pos.facing

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
				if (killOnEnd)
				{
					Global.engine.removeEntity(entity)
				}
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
		out.killOnEnd = killOnEnd
		out.entityPerTile = entityPerTile

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

class ScreenShakeAction() : AbstractTimelineAction()
{
	var speed: Float = 0f
	var amount: Float = 0f

	override fun enter()
	{
		Global.engine.render().renderer.setScreenShake(amount, speed)
		Global.engine.render().renderer.lockScreenShake()
	}

	override fun exit()
	{
		Global.engine.render().renderer.unlockScreenShake()
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = ScreenShakeAction()
		out.parent = parent

		out.startTime = startTime
		out.duration = duration
		out.speed = speed
		out.amount = amount

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		this.speed = 1f / xml.getFloat("Speed", 10f)
		this.amount = xml.getFloat("Strength")
	}
}
