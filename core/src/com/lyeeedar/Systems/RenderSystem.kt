package com.lyeeedar.Systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.SpaceSlot
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Colour

class RenderSystem(): EntitySystem(systemList.indexOf(RenderSystem::class))
{
	val batchHDRColour: HDRColourSpriteBatch = HDRColourSpriteBatch()
	lateinit var entities: ImmutableArray<Entity>

	val tempCol = Colour()

	val tileSize = 32f

	lateinit var renderer: SortedRenderer// = SortedRenderer(tileSize, 200f, 200f, SpaceSlot.Values.size)

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

	init
	{
		DebugConsole.register("DebugDraw", "'DebugDraw speed' to enable, 'DebugDraw false' to disable", fun (args, console): Boolean {
			if (args[0] == "false")
			{
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

					return true
				}
				catch (ex: Exception)
				{
					console.error(ex.message!!)
					return false
				}
			}
		})
	}

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(RenderableComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
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

			val tileCol = tempCol.set(Colour.WHITE)

			if (tile != null)
			{
				if (!tile.isSeen) continue

				tileCol.set(tile.light)

				if (!tile.isVisible)
				{
					// dont draw dynamic entities on non visible tiles
					val task = Mappers.task.get(entity)
					if (task != null) continue

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
		}

		batchHDRColour.begin()
		renderer.flush(batchHDRColour)
		batchHDRColour.end()
	}
}