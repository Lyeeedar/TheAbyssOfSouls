package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import kotlin.reflect.KClass

/**
 * Created by Philip on 20-Mar-16.
 */

class Mappers
{
	companion object
	{
		@JvmField val position: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
		@JvmField val sprite: ComponentMapper<SpriteComponent> = ComponentMapper.getFor(SpriteComponent::class.java)
		@JvmField val tilingSprite: ComponentMapper<TilingSpriteComponent> = ComponentMapper.getFor(TilingSpriteComponent::class.java)
		@JvmField val task: ComponentMapper<TaskComponent> = ComponentMapper.getFor(TaskComponent::class.java)
		@JvmField val light: ComponentMapper<LightComponent> = ComponentMapper.getFor(LightComponent::class.java)
		@JvmField val occluder: ComponentMapper<OccluderComponent> = ComponentMapper.getFor(OccluderComponent::class.java)
	}
}