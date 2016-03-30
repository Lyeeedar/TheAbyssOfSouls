package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.SpriteComponent

/**
 * Created by Philip on 20-Mar-16.
 */

class SpriteUpdaterSystem(): IteratingSystem (Family.all(SpriteComponent::class.java).get(), systemList.indexOf(SpriteUpdaterSystem::class))
{
	override fun processEntity(entity: Entity, deltaTime: Float)
	{
		val sprite = Mappers.sprite.get(entity)
		sprite.sprite.update(deltaTime)
	}
}