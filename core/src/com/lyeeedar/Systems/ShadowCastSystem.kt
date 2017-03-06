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
	private val jobs = Array<Job>()
	
	override fun doUpdate(deltaTime: Float)
	{
		jobs.clear()
		
		runBlocking {
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

				if (shadow != null && stats != null)
				{
					val job = launch(CommonPool) {
						shadow.cache.getShadowCast(tile.level.grid, tile.x, tile.y, stats.sight.toInt(), entity)
					}
					jobs.add(job)
				}
				
				if (light != null) 
				{
					val job = launch(CommonPool) {
						light.cache.getShadowCast(tile.level.grid, tile.x, tile.y, Math.ceil(light.dist.toDouble()).toInt(), entity)
					}
					jobs.add(job)
				}
			}

			for (job in jobs) job.Join()
		}
	}
}
