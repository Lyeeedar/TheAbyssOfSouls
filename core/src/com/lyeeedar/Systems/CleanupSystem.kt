package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.EffectComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.StatisticsComponent
import com.lyeeedar.Level.Tile

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
		val stats = Mappers.stats.get(entity)
		if (stats != null && stats.hp < 0)
		{
			// only cleanup if tile has no effects
			val sprite = Mappers.sprite.get(entity)
			if (sprite?.sprite?.spriteAnimation != null) return

			val pos = Mappers.position.get(entity)
			if (pos?.position is Tile)
			{
				val tile = (pos.position as Tile)

				if (tile.effects.size != 0) return

				tile.contents.remove(pos.slot)
			}

			eng.removeEntity(entity)
		}

		val effect = Mappers.effect.get(entity)
		if (effect != null && effect.completed)
		{
			val event = Mappers.event.get(entity)
			if (event == null || event.pendingEvents.size == 0)
			{
				val pos = Mappers.position.get(entity)
				if (pos?.position is Tile)
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