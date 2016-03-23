package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AssetManager
import com.lyeeedar.Enums
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
fun Entity.sprite() = Mappers.sprite.get(this)
fun Entity.renderOffset() = this.sprite()?.sprite?.spriteAnimation?.renderOffset

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
		@JvmField val effect: ComponentMapper<EffectComponent> = ComponentMapper.getFor(EffectComponent::class.java)
	}
}

class EntityLoader()
{
	companion object
	{
		@JvmStatic fun load(path: String): Entity
		{
			val xml = XmlReader().parse(Gdx.files.internal("Entities/$path.xml"))
			return load(xml)
		}

		@JvmStatic fun load(xml: XmlReader.Element): Entity
		{
			val entity = if (xml.getAttribute("Extends", null) != null) load(xml.getAttribute("Extends")) else Entity()

			entity.add(NameComponent(xml.get("Name")))

			val ai = xml.get("AI", null)
			if (ai != null) entity.add(TaskComponent(ai))

			val pos = entity.position() ?: PositionComponent()
			entity.add(pos)

			val slot = xml.get("Slot", null)
			if (slot != null) pos.slot = Enums.SpaceSlot.valueOf(slot.toUpperCase())

			val size = xml.getInt("Size", -1)
			if (size != -1) pos.size = size

			val sprite = xml.getChildByName("Sprite")
			if (sprite != null) entity.add(SpriteComponent(AssetManager.loadSprite(sprite)))

			val tilingSprite = xml.getChildByName("TilingSprite")
			if (tilingSprite != null) entity.add(TilingSpriteComponent(AssetManager.loadTilingSprite(tilingSprite)))

			val light = xml.getChildByName("Light")
			if (light != null) entity.add(LightComponent(AssetManager.loadColour(light.getChildByName("Colour")), light.getFloat("Distance")))

			val occludes = xml.getBoolean("Occludes", false)
			if (occludes) entity.add(OccluderComponent())

			val statistics = xml.getChildByName("Statistics")
			val factions = xml.getChildByName("Factions")

			if (statistics != null || factions != null)
			{
				val stats = entity.stats() ?: StatisticsComponent()
				entity.add(stats)

				if (statistics != null)
				{
					Enums.Statistic.load(statistics, stats.stats)
					stats.hp = stats.stats.get(Enums.Statistic.MAX_HEALTH)
					stats.stamina = stats.stats.get(Enums.Statistic.MAX_STAMINA)
				}

				if (factions != null)
				{
					if (factions.getBooleanAttribute("Override", false)) stats.factions.clear()
					stats.factions.addAll(factions.text.toLowerCase().split(","))
				}
			}

			val eventsEl = xml.getChildByName("Events")
			if (eventsEl != null)
			{
				val events = entity.event() ?: EventComponent()
				entity.add(events)
				events.parse(eventsEl)
			}

			return entity
		}
	}
}