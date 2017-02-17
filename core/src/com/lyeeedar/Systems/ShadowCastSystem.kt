package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.*
import com.lyeeedar.Statistic

class ShadowCastSystem(): IteratingSystem(Family.all(PositionComponent::class.java).one(ShadowCastComponent::class.java, LightComponent::class.java).get(), systemList.indexOf(ShadowCastSystem::class))
{
	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val tile = entity?.tile() ?: return
		val stats = Mappers.stats.get(entity)
		val shadow = Mappers.shadow.get(entity)
		val light = Mappers.light.get(entity)

		if (shadow != null && stats != null) shadow.cache.getShadowCast(tile.level.grid, tile.x, tile.y, stats.stats.get(Statistic.SIGHT).toInt(), entity)
		if (light != null) light.cache.getShadowCast(tile.level.grid, tile.x, tile.y, Math.ceil(light.dist.toDouble()).toInt(), entity)
	}
}