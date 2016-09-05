package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot

/**
 * Created by Philip on 21-Mar-16.
 */

class CleanupSystem(): IteratingSystem(Family.one(StatisticsComponent::class.java, EffectComponent::class.java).get(), systemList.indexOf(CleanupSystem::class))
{
	lateinit var eng: Engine


	override fun addedToEngine(engine: Engine?)
	{
		this.eng = engine ?: return

		super.addedToEngine(engine)
	}

	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val level = Global.currentLevel
		val pos = entity?.pos()!!
		var visible = false
		for (x in pos.min.x..pos.max.x)
		{
			for (y in pos.min.y..pos.max.y)
			{
				val tile = level.getTile(x, y) ?: continue
				if (tile.visible)
				{
					visible = true
					break
				}
			}
		}

		val stats = Mappers.stats.get(entity)
		if (stats != null && stats.hp <= 0)
		{
			if (visible)
			{
				// only cleanup if tile has no effects
				val sprite = Mappers.sprite.get(entity)
				if (sprite?.sprite?.animation != null) return
			}

			val tasks = Mappers.task.get(entity)
			tasks.ai.cancel()

			if (pos.position is Tile)
			{
				val tile = (pos.position as Tile)

				if (pos.hasEffects()) return

				for (x in 0..pos.size-1)
				{
					for (y in 0..pos.size-1)
					{
						tile.level.getTile(tile, x, y)?.contents?.remove(pos.slot)
					}
				}

				if (pos.slot == SpaceSlot.WALL)
				{
					tile.level.recalculateGrids()
				}
			}

			eng.removeEntity(entity)
		}

		val effect = Mappers.effect.get(entity)
		if (effect != null)
		{
			val event = Mappers.event.get(entity)

			// if not visible fire remaining
			if (!visible)
			{
				for (stage in Sprite.AnimationStage.Values)
				{
					val e = effect.eventMap[stage]
					if (e != null) entity?.postEvent(e)
				}

				// process remaining events
				eng.getSystem(EventSystem::class.java).processEntity(entity)

				effect.completed = true
			}

			if (effect.completed && (event == null || event.pendingEvents.size == 0))
			{
				if (pos.position is Tile)
				{
					val tile = (pos.position as Tile)

					for (x in pos.min.x..pos.max.x)
					{
						for (y in pos.min.y..pos.max.y)
						{
							tile.level.getTile(x, y)?.effects?.removeValue(entity, true)
						}
					}
				}

				eng.removeEntity(entity)
			}

		}
	}

}