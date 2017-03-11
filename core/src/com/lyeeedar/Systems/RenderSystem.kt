package com.lyeeedar.Systems

import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.floor
import com.lyeeedar.Util.lerp

class RenderSystem(): AbstractSystem(Family.all(PositionComponent::class.java).one(RenderableComponent::class.java).get())
{
	val shape: ShapeRenderer by lazy { ShapeRenderer() }
	var drawParticleDebug = false
	var drawEmitters = false
	var drawParticles = false
	val particles = Array<ParticleEffect>()

	val batchHDRColour: HDRColourSpriteBatch = HDRColourSpriteBatch()

	val tileCol = Colour()
	val hpBarCol = Colour()

	var tileSize = 40f
		set(value)
		{
			field = value
			renderer.tileSize = value
		}

	var isAnimatingTileSize = false
	var startTileSize = 0f
	var targetTileSize = 0f
	var tileSizeProgress = 0f
	var tileSizeDuration = 0f

	val hp_full_green = AssetManager.loadTextureRegion("Sprites/GUI/health_full_green.png")!!
	val hp_full_red = AssetManager.loadTextureRegion("Sprites/GUI/health_full.png")!!
	val hp_full_blue = AssetManager.loadTextureRegion("Sprites/GUI/health_full_blue.png")!!
	val hp_empty = AssetManager.loadTextureRegion("Sprites/GUI/health_empty.png")!!

	lateinit var renderer: SortedRenderer
	val screenSpaceRenderer = SortedRenderer(Global.resolution[1].toFloat(), 1f, 1f, 1)

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

	fun setTileSizeAnimated(targetSize: Float, duration: Float)
	{
		startTileSize = tileSize
		targetTileSize = targetSize
		tileSizeProgress = 0f
		tileSizeDuration = duration

		isAnimatingTileSize = true
	}

	override fun onLevelChanged()
	{
		if (level != null)
		{
			renderer = SortedRenderer(tileSize, level!!.width.toFloat(), level!!.height.toFloat(), SpaceSlot.Values.size)
		}
	}

	override fun doUpdate(deltaTime: Float)
	{
		if (level == null) return

		if (isAnimatingTileSize)
		{
			tileSizeProgress += deltaTime

			tileSize = startTileSize.lerp(targetTileSize, tileSizeProgress / tileSizeDuration)

			if (tileSizeProgress > tileSizeDuration)
			{
				tileSize = targetTileSize
				isAnimatingTileSize = false
			}
		}

		val player = level!!.player
		val playerPos = player.pos()
		val playerSprite = player.renderable().renderable
		renderer.update(playerSprite, deltaTime)

		var offsetx = Global.resolution.x / 2 - playerPos.position.x * tileSize - tileSize / 2
		var offsety = Global.resolution.y / 2 - playerPos.position.y * tileSize - tileSize / 2

		val offset = playerSprite.animation?.renderOffset()
		if (offset != null)
		{
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		renderer.begin(deltaTime, offsetx, offsety)

		for (entity in entities)
		{
			val renderable = entity.renderable().renderable ?: continue
			val pos = entity.pos() ?: continue
			val tile = entity.tile()

			val px = pos.position.x.toFloat()
			val py = pos.position.y.toFloat()

			if (pos.position.taxiDist(level!!.player.tile()!!) > 50)
			{
				renderable.animation = null

				if (renderable is ParticleEffect)
				{
					if (entity.components.size() == 2)
					{
						entity.add(MarkedForDeletionComponent())
					}
				}

				//renderer.update(renderable)
				continue
			}

			if (renderable is ParticleEffect)
			{
				val effect = renderable
				if (effect.completed && effect.complete() && entity.components.size() == 2)
				{
 					entity.add(MarkedForDeletionComponent())
				}
			}

			tileCol.set(Colour.WHITE)

			if (tile != null)
			{
				if (!tile.isSeen)
				{
					renderable.animation = null

					if (renderable is TilingSprite)
					{
						renderer.addToMap(renderable, px, py)
					}
					continue
				}

				tileCol.set(tile.light)

				if (!tile.isVisible)
				{
					// dont draw dynamic entities on non visible tiles
					if (entity.task() != null || entity.trailing() != null || entity.sceneTimeline() != null)
					{
						renderable.animation = null
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

			renderer.queue(renderable, px, py, pos.slot.ordinal, 0, tileCol)

			if (drawParticleDebug && renderable is ParticleEffect)
			{
				particles.add(renderable)
			}

			val offset = renderable.animation?.renderOffset()

			var ax = px
			var ay = py

			if (offset != null)
			{
				ax += offset[0]
				ay += offset[1]
			}

			val additional = entity.additionalRenderable()
			if (additional != null)
			{
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

			val stats = entity.stats()
			if (stats != null && Global.interaction == null && stats.showHp)
			{
				val hp_full = if (entity == player) hp_full_green else hp_full_red

				val totalWidth = pos.size.toFloat()

				val hp = stats.hp.toInt()
				val maxhp = stats.maxHP.toInt()

				val solidSpaceRatio = 0.05f
				val space = totalWidth
				val spacePerPip = space / maxhp
				val spacing = spacePerPip * solidSpaceRatio
				val solid = spacePerPip - spacing

				var overhead = totalWidth
				if (renderable is Sprite && renderable.drawActualSize)
				{
					val region = renderable.currentTexture
					val height = region.regionHeight
					val ratio = height / 32f
					overhead *= ratio
				}

				for (i in 0..maxhp-1)
				{
					val pip: TextureRegion

					if (i >= hp && i < hp+stats.regeneratingHP.floor()) pip = hp_full_red
					else if(i < hp) pip = hp_full
					else pip = hp_empty

					renderer.queueTexture(pip, ax+i*spacePerPip, ay+overhead, pos.slot.ordinal, 1, colour = tileCol, width = solid, height = 0.1f, sortX = ax, sortY = ay)
				}

				if (entity == player)
				{
					val stamina = stats.stamina.toInt()
					val maxstamina = stats.maxStamina.toInt()

					val solidSpaceRatio = 0.05f
					val space = totalWidth
					val spacePerPip = space / maxstamina
					val spacing = spacePerPip * solidSpaceRatio
					val solid = spacePerPip - spacing

					for (i in 0..maxstamina-1)
					{
						var pip = if(i < stamina) hp_full_blue else hp_empty
						if (entity.stats().insufficientStamina > 0f)
						{
							entity.stats().insufficientStamina -= deltaTime
							pip = if(i < entity.stats().insufficientStaminaAmount) hp_full_red else hp_empty
						}

						renderer.queueTexture(pip, ax+i*spacePerPip, ay+0.1f, pos.slot.ordinal, 1, colour = tileCol, width = solid, height = 0.1f)
					}
				}
			}
		}

		batchHDRColour.begin()

		renderer.flush(batchHDRColour)

		screenSpaceRenderer.begin(deltaTime, 0f, 0f)

		val screenspaceItr = level!!.screenSpaceEffects.iterator()
		while (screenspaceItr.hasNext())
		{
			val effect = screenspaceItr.next()

			screenSpaceRenderer.queueParticle(effect, 0f, 0f, 0, 0)

			if (!effect.loop && effect.complete() && effect.completed)
			{
				screenspaceItr.remove()
			}
		}

		screenSpaceRenderer.flush(batchHDRColour)

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
	}
}