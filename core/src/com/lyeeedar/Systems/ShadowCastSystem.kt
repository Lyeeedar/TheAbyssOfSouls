package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Components.ShadowCastComponent
import com.lyeeedar.Components.StatisticsComponent
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile

/**
 * Created by Philip on 21-Mar-16.
 */

class ShadowCastSystem(): IteratingSystem(Family.all(ShadowCastComponent::class.java, PositionComponent::class.java, StatisticsComponent::class.java).get())
{
	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val pos = Mappers.position.get(entity)
		val tile = pos.position as? Tile ?: return
		val stats = Mappers.stats.get(entity)
		var cache = Mappers.shadow.get(entity)

		cache.cache.getShadowCast(tile.level.grid, tile.x, tile.y, stats.stats.get(Enums.Statistic.SIGHT).toInt(), entity)
	}

}
