package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.*
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Items.Item
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.isAllies
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
fun Entity.renderOffset() = this.sprite()?.sprite?.spriteAnimation?.renderOffset
fun Entity.getEquip(slot: EquipmentSlot) = Mappers.inventory.get(this).equipment.get(slot)

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
		@JvmField val telegraphed: ComponentMapper<TelegraphedAttackComponent> = ComponentMapper.getFor(TelegraphedAttackComponent::class.java)
		@JvmField val readyAttack: ComponentMapper<ReadyAttackComponent> = ComponentMapper.getFor(ReadyAttackComponent::class.java)
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

			val name = xml.get("Name", null)
			if (name != null) entity.add(NameComponent(name))

			val ai = xml.get("AI", null)
			if (ai != null) entity.add(TaskComponent(ai))

			val leader = xml.get("Leader", null)
			if (leader != null) Mappers.task.get(entity).leaderName = leader

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

			val light = xml.getChildByName("Light")
			if (light != null) entity.add(LightComponent(AssetManager.loadColour(light.getChildByName("Colour")), light.getFloat("Distance")))

			val occludes = xml.getBoolean("Occludes", false)
			if (occludes) entity.add(OccluderComponent())

			val statistics = xml.getChildByName("Statistics")
			val factions = xml.getChildByName("Factions")
			val attack = xml.getChildByName("Attack")
			val defense = xml.getChildByName("Defense")

			if (statistics != null || factions != null || attack != null || defense != null)
			{
				val stats = entity.stats() ?: StatisticsComponent()
				entity.add(stats)

				if (statistics != null)
				{
					Statistic.load(statistics, stats.stats)
					stats.stats[Statistic.HEALTH] = stats.stats[Statistic.MAX_HEALTH]
					stats.stats[Statistic.STAMINA] = stats.stats[Statistic.MAX_STAMINA]
				}

				if (factions != null)
				{
					if (factions.getBooleanAttribute("Override", false)) stats.factions.clear()
					val split = factions.text.toLowerCase().split(",")
					for (faction in split) stats.factions.add(faction)
				}

				if (attack != null)
				{
					stats.attack.addAll(ElementType.load(attack))
				}

				if (defense != null)
				{
					stats.defense.addAll(ElementType.load(defense))
				}
			}

			val eventsEl = xml.getChildByName("Events")
			if (eventsEl != null)
			{
				val events = entity.event() ?: EventComponent()
				entity.add(events)
				events.parse(eventsEl)
			}

			val telegraphedEl = xml.getChildByName("TelegraphedAttacks")
			if (telegraphedEl != null)
			{
				val telegraph = TelegraphedAttackComponent()
				telegraph.parse(telegraphedEl)
				entity.add(telegraph)
			}

			val inventory = xml.getChildByName("Inventory")
			if (inventory != null)
			{
				val inv = InventoryComponent()

				val equipment = inventory.getChildByName("Equipment")
				if (equipment != null)
				{
					for (i in 0..equipment.childCount-1)
					{
						val el = equipment.getChild(i)
						val item = Item.load(el)

						inv.equipment.put(item.slot, item)
					}
				}

				for (i in 0..inventory.childCount-1)
				{
					val el = inventory.getChild(i)

					if (el == equipment) continue

					inv.items.add(Item.load(el))
				}

				entity.add(inv)
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