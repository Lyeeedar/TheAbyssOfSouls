package com.lyeeedar.Systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Colour

class RenderSystem(): EntitySystem(systemList.indexOf(RenderSystem::class))
{
	val shape: ShapeRenderer by lazy { ShapeRenderer() }
	var drawParticleDebug = false
	var drawEmitters = false
	var drawParticles = false
	val particles = Array<ParticleEffect>()

	val batchHDRColour: HDRColourSpriteBatch = HDRColourSpriteBatch()
	lateinit var entities: ImmutableArray<Entity>

	val tempCol = Colour()

	val tileSize = 40f

	lateinit var renderer: SortedRenderer

	var level: Level? = null
		get() = field
		set(value)
		{
			field = value

			if (value != null)
			{
				renderer = SortedRenderer(tileSize, value.width.toFloat(), value.height.toFloat(), SpaceSlot.Values.size)
			}
		}

	var processDuration: Float = 0f

	init
	{
		DebugConsole.register("DebugDraw", "'DebugDraw speed' to enable, 'DebugDraw false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
				console.write("Debug draw disabled")

				renderer.debugDraw = false
				return true
			}
			else
			{
				try
				{
					val speed = args[0].toFloat()
					renderer.debugDraw = true
					renderer.debugDrawSpeed = speed

					console.write("Debug draw enabled")

					return true
				}
				catch (ex: Exception)
				{
					console.error(ex.message!!)
					return false
				}
			}
		})

		DebugConsole.register("ParticleDebug", "'ParticleDebug (Emitter|Particle) true' to enable, 'ParticleDebug (Emitter|Particle) false' to disable", fun (args, console): Boolean {
			if (args[0] == "false" && args.size == 1)
			{
				console.write("Particle debug draw disabled")

				drawEmitters = false
				drawParticles = false
				drawParticleDebug = false
				return true
			}
			else if (args[0] == "true" && args.size == 1)
			{
				console.write("Particle debug draw enabled")

				drawEmitters = true
				drawParticles = true
				drawParticleDebug = true
				return true
			}
			else
			{
				val changingEmitter = args.contains("emitter")
				val changingParticle = args.contains("particle")

				val isTrue = args.contains("true")
				val isFalse = args.contains("false")

				if (isTrue == isFalse || !(changingEmitter || changingParticle))
				{
					return false
				}

				if (changingEmitter) drawEmitters = isTrue
				if (changingParticle) drawParticles = isTrue

				console.write("Enable particle debug")
				return true
			}
		})
	}

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(RenderableComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val start = System.nanoTime()

		if (level == null) return

		val player = level!!.player
		val playerPos = player.pos()
		val playerSprite = player.renderable()
		renderer.update(playerSprite.renderable, deltaTime)

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * tileSize - tileSize / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * tileSize - tileSize / 2

		val offset = playerSprite.renderable.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		renderer.begin(deltaTime, offsetx, offsety)

		for (entity in entities)
		{
			val renderable = entity.renderable()
			val pos = entity.pos() ?: continue
			val tile = entity.tile()

			if (pos.position.taxiDist(level!!.player.tile()!!) > 100)
			{
				renderer.update(renderable.renderable)

				continue
			}

			if (renderable.renderable is ParticleEffect)
			{
				val effect = renderable.renderable as ParticleEffect
				if (effect.complete() && entity.components.size() == 2)
				{
					engine.removeEntity(entity)
				}
			}

			val tileCol = tempCol.set(Colour.WHITE)

			if (tile != null)
			{
				if (!tile.isSeen)
				{
					renderable.renderable.animation = null
				}

				tileCol.set(tile.light)

				if (!tile.isVisible)
				{
					// dont draw dynamic entities on non visible tiles
					val task = Mappers.task.get(entity)
					if (task != null)
					{
						renderable.renderable.animation = null
						continue
					}

					tileCol.mul(0.6f, 0.4f, 0.8f, 1.0f)
				}

				if (tile.isSelectedPoint)
				{
					tileCol.mul(0.6f, 1.2f, 0.6f, 1.0f)
				}
				else
				{
					if (tile.isValidTarget) tileCol.mul(0.65f, 0.65f, 0.4f, 1.0f)
					if (tile.isValidHitPoint) tileCol.mul(1.2f, 0.7f, 0.7f, 1.0f)
				}
			}

			val px = pos.position.x.toFloat()
			val py = pos.position.y.toFloat()

			renderer.queue(renderable.renderable, px, py, pos.slot.ordinal, 0, tileCol)

			if (drawParticleDebug && renderable.renderable is ParticleEffect)
			{
				particles.add(renderable.renderable as ParticleEffect)
			}

			val additional = entity.additionalRenderable()
			if (additional != null)
			{
				val offset = renderable.renderable.animation?.renderOffset()

				var ax = px
				var ay = py

				if (offset != null)
				{
					ax += offset[0]
					ay += offset[1]
				}

				for (below in additional.below.values())
				{
					renderer.queue(below, ax, ay, pos.slot.ordinal, -1, tileCol)

					if (drawParticleDebug && below is ParticleEffect)
					{
						particles.add(below)
					}

				}

				for (above in additional.above.values())
				{
					renderer.queue(above, ax, ay, pos.slot.ordinal, 1, tileCol)

					if (drawParticleDebug && above is ParticleEffect)
					{
						particles.add(above)
					}
				}
			}
		}

		batchHDRColour.begin()
		renderer.flush(batchHDRColour)
		batchHDRColour.end()

		if (drawParticleDebug)
		{
			shape.projectionMatrix = Global.stage.camera.combined
			shape.setAutoShapeType(true)
			shape.begin()

			for (particle in particles)
			{
				particle.debug(shape, offsetx, offsety, tileSize, drawEmitters, drawParticles)
			}

			shape.end()

			particles.clear()
		}

		val end = System.nanoTime()
		val diff = (end - start) / 1000000000f

		processDuration = (processDuration + diff) / 2f
	}
}