package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.DirectionalSpriteComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Components.SpriteComponent
import com.lyeeedar.Direction
import com.lyeeedar.Renderables.Sprite.DirectionalSprite

class DirectionalSpriteSystem(): IteratingSystem(Family.all(DirectionalSpriteComponent::class.java, PositionComponent::class.java).get(), systemList.indexOf(DirectionalSpriteSystem::class))
{
	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		if (entity == null) return

		val pos = Mappers.position.get(entity)
		val dirSprite = Mappers.directionalSprite.get(entity)

		var sprite = Mappers.sprite.get(entity)
		if (sprite == null)
		{
			sprite = SpriteComponent()
			entity.add(sprite)
		}

		if (pos.facing == Direction.SOUTH)
		{
			dirSprite.lastV = DirectionalSprite.VDir.DOWN
		}
		else if (pos.facing == Direction.NORTH)
		{
			dirSprite.lastV = DirectionalSprite.VDir.UP
		}
		else if (pos.facing == Direction.EAST)
		{
			dirSprite.lastH = DirectionalSprite.HDir.RIGHT
		}
		else if (pos.facing == Direction.WEST)
		{
			dirSprite.lastH = DirectionalSprite.HDir.LEFT
		}

		val chosen = dirSprite.directionalSprite.getSprite(dirSprite.currentAnim, dirSprite.lastV, dirSprite.lastH)

		if (chosen != sprite.sprite)
		{
			chosen.animation = sprite.sprite.animation
			sprite.sprite = chosen
		}
	}
}