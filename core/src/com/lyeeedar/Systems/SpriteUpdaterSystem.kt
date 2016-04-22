package com.lyeeedar.Systems

import com.badlogic.ashley.core.*
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.AnimationComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.SpriteComponent
import com.lyeeedar.Components.sprite

/**
 * Created by Philip on 20-Mar-16.
 */

class SpriteUpdaterSystem(): IteratingSystem (Family.all(SpriteComponent::class.java, AnimationComponent::class.java).get(), systemList.indexOf(SpriteUpdaterSystem::class))
{
	override fun addedToEngine(engine: Engine?)
	{
		super.addedToEngine(engine)

		engine?.addEntityListener(Family.all(SpriteComponent::class.java).get(), SpriteListener())
	}

	override fun processEntity(entity: Entity, deltaTime: Float)
	{
		val sprite = Mappers.sprite.get(entity)
		sprite.sprite.update(deltaTime)
	}

	class SpriteListener(): EntityListener
	{
		override fun entityRemoved(entity: Entity?)
		{

		}

		override fun entityAdded(entity: Entity?)
		{
			val sprite = entity!!.sprite()!!

			if (sprite.sprite.textures.size > 1 || sprite.sprite.spriteAnimation != null)
			{
				entity.add(AnimationComponent())
			}
		}
	}
}