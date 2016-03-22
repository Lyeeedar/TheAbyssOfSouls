package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Level.Tile
import kotlin.reflect.KClass

/**
 * Created by Philip on 20-Mar-16.
 */

fun Entity.position() = Mappers.position.get(this)
fun Entity.tile() = Mappers.position.get(this).position as? Tile
fun Entity.stats() = Mappers.stats.get(this)
fun Entity.event() = Mappers.event.get(this)
fun Entity.postEvent(args:EventArgs) = this.event()?.pendingEvents?.add(args)
fun Entity.name() = Mappers.name.get(this).name

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
		@JvmField val stats: ComponentMapper<StatisticsComponent> = ComponentMapper.getFor(StatisticsComponent::class.java)
		@JvmField val shadow: ComponentMapper<ShadowCastComponent> = ComponentMapper.getFor(ShadowCastComponent::class.java)
		@JvmField val event: ComponentMapper<EventComponent> = ComponentMapper.getFor(EventComponent::class.java)
		@JvmField val name: ComponentMapper<NameComponent> = ComponentMapper.getFor(NameComponent::class.java)
	}
}