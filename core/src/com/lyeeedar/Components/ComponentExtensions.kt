package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.*
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Level.Item
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.AssetManager
import kotlin.reflect.KClass

/**
 * Created by Philip on 20-Mar-16.
 */

fun Entity.pos() = Mappers.position.get(this)
fun Entity.tile() = Mappers.position.get(this).position as? Tile
fun Entity.stats() = Mappers.stats.get(this)
fun Entity.event() = Mappers.event.get(this)
fun Entity.postEvent(args:EventArgs) = this.event()?.pendingEvents?.add(args)
fun Entity.name() = Mappers.name.get(this)?.name ?: ""
fun Entity.sprite() = Mappers.sprite.get(this)
fun Entity.renderOffset() = this.sprite()?.sprite?.animation?.renderOffset()
fun Entity.task() = Mappers.task.get(this)
fun Entity.combo() = Mappers.combo.get(this)

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
		@JvmField val inventory: ComponentMapper<InventoryComponent> = ComponentMapper.getFor(InventoryComponent::class.java)
		@JvmField val directionalSprite: ComponentMapper<DirectionalSpriteComponent> = ComponentMapper.getFor(DirectionalSpriteComponent::class.java)
		@JvmField val combo: ComponentMapper<ComboComponent> = ComponentMapper.getFor(ComboComponent::class.java)
		@JvmField val particle: ComponentMapper<ParticleComponent> = ComponentMapper.getFor(ParticleComponent::class.java)
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
			val entity = if (xml.get("Extends", null) != null) load(xml.get("Extends")) else Entity()

			val name = xml.get("Name", null)
			if (name != null) entity.add(NameComponent(name))

			val ai = xml.get("AI", null)
			if (ai != null) entity.add(TaskComponent(ai))

			val pos = entity.pos() ?: PositionComponent()
			entity.add(pos)

			val slot = xml.get("Slot", null)
			if (slot != null) pos.slot = SpaceSlot.valueOf(slot.toUpperCase())

			val canSwap = xml.get("CanSwap", null)
			if (canSwap != null) pos.canSwap = xml.getBoolean("CanSwap")

			val size = xml.getInt("Size", -1)
			if (size != -1) pos.size = size

			val sprite = xml.getChildByName("Sprite")
			if (sprite != null) entity.add(SpriteComponent(AssetManager.loadSprite(sprite)))

			val tilingSprite = xml.getChildByName("TilingSprite")
			if (tilingSprite != null) entity.add(TilingSpriteComponent(AssetManager.loadTilingSprite(tilingSprite)))

			val directionalSprite = xml.getChildByName("DirectionalSprite")
			if (directionalSprite != null) entity.add(DirectionalSpriteComponent(AssetManager.loadDirectionalSprite(directionalSprite)))

			val light = xml.getChildByName("Light")
			if (light != null) entity.add(LightComponent(AssetManager.loadColour(light.getChildByName("Colour")), light.getFloat("Distance")))

			val occludes = xml.getBoolean("Occludes", false)
			if (occludes) entity.add(OccluderComponent())

			val eventsEl = xml.getChildByName("Events")
			if (eventsEl != null)
			{
				val events = entity.event() ?: EventComponent()
				entity.add(events)
				events.parse(eventsEl)
			}

			entity.add(StatisticsComponent())

			val comboEl = xml.getChildByName("Combo")
			if (comboEl != null)
			{
				val trees = ComboTree.load(comboEl)

				val combo = ComboComponent()
				combo.combos.addAll(trees)

				entity.add(combo)
			}

			return entity
		}
	}
}

fun Entity.isAllies(other: Entity): Boolean { return if (this.stats() != null && other.stats() != null) this.stats().factions.isAllies(other.stats().factions) else false }

fun Entity.getEdgeTiles(dir: Direction): com.badlogic.gdx.utils.Array<Tile>
{
	val pos = this.pos()
	val tile = this.tile() ?: throw RuntimeException("argh tile is null")

	var xstep = 0;
	var ystep = 0;

	var sx = 0;
	var sy = 0;

	if ( dir == Direction.NORTH )
	{
		sx = 0;
		sy = pos.size - 1;

		xstep = 1;
		ystep = 0;
	}
	else if ( dir == Direction.SOUTH )
	{
		sx = 0;
		sy = 0;

		xstep = 1;
		ystep = 0;
	}
	else if ( dir == Direction.EAST )
	{
		sx = pos.size - 1;
		sy = 0;

		xstep = 0;
		ystep = 1;
	}
	else if ( dir == Direction.WEST )
	{
		sx = 0;
		sy = 0;

		xstep = 0;
		ystep = 1;
	}

	val tiles = com.badlogic.gdx.utils.Array<Tile>()
	for (i in 0..pos.size-1)
	{
		val t = tile.level.getTile(tile, sx + xstep * i, sy + ystep * i) ?: continue
		tiles.add(t)
	}

	return tiles
}