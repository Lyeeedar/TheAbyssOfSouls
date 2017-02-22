package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.lyeeedar.Components.*
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import ktx.collections.set

class TileSystem : EntitySystem(systemList.indexOf(TileSystem::class))
{
	var level: Level? = null
		get() = field
		set(value)
		{
			field = value
		}

	var processDuration: Float = 0f

	override fun update(deltaTime: Float)
	{
		val start = System.nanoTime()

		for (tile in level!!.grid)
		{
			processWater(tile)
		}

		val end = System.nanoTime()
		val diff = (end - start) / 1000000000f

		processDuration = (processDuration + diff) / 2f
	}

	fun processWater(tile: Tile)
	{
		val entity = tile.contents[SpaceSlot.ENTITY] ?: return
		val pos = entity.pos() ?: return

		val sprite = entity.renderable()?.renderable as? Sprite ?: return

		val water = tile.contents[SpaceSlot.FLOOR]?.water()

		if (water == null)
		{
			val additional = entity.additionalRenderable()
			if (additional != null)
			{
				val ripple = additional.below["WaterRipple", null] as? ParticleEffect

				if (ripple != null)
				{
					additional.below.remove("WaterRipple")

					ripple.stop()

					val rippleEnt = Entity()
					rippleEnt.add(RenderableComponent(ripple))

					val entPos = PositionComponent()
					entPos.position = pos.position
					entPos.slot = pos.slot

					rippleEnt.add(entPos)

					engine.addEntity(rippleEnt)
				}
			}

			sprite.removeAmount = 0.0f
			return
		}

		if (sprite.animation == null)
		{
			if (sprite.removeAmount == 0f)
			{
				sprite.removeAmount = water.depth / pos.size

				if (water.depth > 0.4f)
				{
					val additional = entity.additionalRenderable() ?: AdditionalRenderableComponent()

					if (!additional.below.containsKey("WaterRipple"))
					{
						val ripple = AssetManager.loadParticleEffect("WaterRipple")
						ripple.size[0] = pos.size
						ripple.size[1] = pos.size

						additional.below["WaterRipple"] = ripple
					}

					entity.add(additional)
				}
			}
		}
	}
}
