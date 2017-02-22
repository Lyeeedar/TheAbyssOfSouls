package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.Level.Level

class ShadowCastSystem(): EntitySystem(systemList.indexOf(ShadowCastSystem::class))
{
	lateinit var entities: ImmutableArray<Entity>

	var level: Level? = null
		get() = field
		set(value)
		{
			field = value
		}

	var processDuration: Float = 0f

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(PositionComponent::class.java).one(ShadowCastComponent::class.java, LightComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val start = System.nanoTime()

		for (entity in entities)
		{
			val tile = entity?.tile() ?: continue

			if (tile.taxiDist(level!!.player.tile()!!) > 100)
			{
				continue
			}

			val stats = Mappers.stats.get(entity)
			val shadow = Mappers.shadow.get(entity)
			val light = Mappers.light.get(entity)

			if (shadow != null && stats != null) shadow.cache.getShadowCast(tile.level.grid, tile.x, tile.y, stats.sight.toInt(), entity)
			if (light != null) light.cache.getShadowCast(tile.level.grid, tile.x, tile.y, Math.ceil(light.dist.toDouble()).toInt(), entity)
		}

		val end = System.nanoTime()
		val diff = (end - start) / 1000000000f

		processDuration = (processDuration + diff) / 2f
	}
}