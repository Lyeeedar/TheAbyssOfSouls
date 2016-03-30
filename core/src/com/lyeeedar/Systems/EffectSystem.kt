package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.EffectComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.postEvent

/**
 * Created by Philip on 22-Mar-16.
 */

class EffectSystem(): IteratingSystem(Family.all(EffectComponent::class.java).get(), systemList.indexOf(EffectSystem::class))
{
	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val effect = Mappers.effect.get(entity)

		val completed = effect.sprite.update(deltaTime)
		if (completed)
		{
			effect.completed = true
		}

		val event = effect.eventMap.get(effect.sprite.animationStage)
		if (event != null)
		{
			entity?.postEvent(event)
		}
	}
}
