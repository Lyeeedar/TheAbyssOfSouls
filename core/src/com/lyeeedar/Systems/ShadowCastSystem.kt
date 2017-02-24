package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.Level.Level

class ShadowCastSystem(): AbstractSystem(Family.all(PositionComponent::class.java).one(ShadowCastComponent::class.java, LightComponent::class.java).get())
{
	override fun doUpdate(deltaTime: Float)
	{
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
	}
}