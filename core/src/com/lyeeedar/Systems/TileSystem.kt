package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.AI.Tasks.TaskInterrupt
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import ktx.collections.set

class TileSystem : AbstractSystem()
{
	override fun doUpdate(deltaTime: Float)
	{
		for (tile in level!!.grid)
		{
			processWater(tile)
		}
	}

	override fun onTurn()
	{
		movedByWater.clear()

		for (tile in level!!.grid)
		{
			processWaterTurn(tile)
		}
	}

	fun processWater(tile: Tile)
	{
		val entity = tile.contents[SpaceSlot.ENTITY] ?: return
		val pos = entity.pos() ?: return

		val sprite = entity.renderable()?.renderable as? Sprite ?: return

		val water = tile.contents[SpaceSlot.FLOOR]?.water() ?: tile.contents[SpaceSlot.BELOWENTITY]?.water()

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

	val movedByWater = ObjectSet<Entity>()
	fun processWaterTurn(tile: Tile)
	{
		val entity = tile.contents[SpaceSlot.ENTITY] ?: return
		val pos = entity.pos() ?: return

		if (movedByWater.contains(entity)) return
		movedByWater.add(entity)

		val water = tile.contents[SpaceSlot.FLOOR]?.water() ?: tile.contents[SpaceSlot.BELOWENTITY]?.water() ?: return

		if (water.flowDir != Direction.CENTRE)
		{
			val direction = water.flowDir

			val doFlow = MathUtils.random(1f)
			if (doFlow <= water.flowChance)
			{
				val prev = (pos.position as Tile)
				val next = prev.level.getTile(prev, direction) ?: return

				if (entity.pos().isValidTile(next, entity))
				{
					entity.pos().doMove(next, entity)

					entity.renderable().renderable.animation = MoveAnimation.obtain().set(next, prev, 0.15f)

					entity.task().tasks.add(TaskInterrupt())
				}
			}
		}
	}
}
