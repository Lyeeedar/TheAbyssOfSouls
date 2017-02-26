package com.lyeeedar.Components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import ktx.collections.set
import ktx.collections.toGdxArray

fun Entity.pos() = Mappers.position.get(this)
fun Entity.tile() = Mappers.position.get(this).position as? Tile
fun Entity.stats() = Mappers.stats.get(this)
fun Entity.renderable() = Mappers.renderable.get(this)
fun Entity.additionalRenderable() = Mappers.additionalRenderable.get(this)
fun Entity.renderOffset() = this.renderable()?.renderable?.animation?.renderOffset()
fun Entity.task() = Mappers.task.get(this)
fun Entity.combo() = Mappers.combo.get(this)
fun Entity.sceneTimeline() = Mappers.sceneTimeline.get(this)
fun Entity.shadow() = Mappers.shadow.get(this)
fun Entity.directionalSprite() = Mappers.directionalSprite.get(this)
fun Entity.name() = Mappers.name.get(this)
fun Entity.water() = Mappers.water.get(this)
fun Entity.trailing() = Mappers.trailing.get(this)
fun Entity.event(): EventComponent
{
	var event = Mappers.event.get(this)
	if (event == null)
	{
		event = EventComponent()
		this.add(event)
	}

	return event
}

class Mappers
{
	companion object
	{
		val name: ComponentMapper<NameComponent> = ComponentMapper.getFor(NameComponent::class.java)
		val position: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
		val renderable: ComponentMapper<RenderableComponent> = ComponentMapper.getFor(RenderableComponent::class.java)
		val additionalRenderable: ComponentMapper<AdditionalRenderableComponent> = ComponentMapper.getFor(AdditionalRenderableComponent::class.java)
		val task: ComponentMapper<TaskComponent> = ComponentMapper.getFor(TaskComponent::class.java)
		val light: ComponentMapper<LightComponent> = ComponentMapper.getFor(LightComponent::class.java)
		val occluder: ComponentMapper<OccluderComponent> = ComponentMapper.getFor(OccluderComponent::class.java)
		val impassable: ComponentMapper<ImpassableComponent> = ComponentMapper.getFor(ImpassableComponent::class.java)
		val stats: ComponentMapper<StatisticsComponent> = ComponentMapper.getFor(StatisticsComponent::class.java)
		val shadow: ComponentMapper<ShadowCastComponent> = ComponentMapper.getFor(ShadowCastComponent::class.java)
		val combo: ComponentMapper<ComboComponent> = ComponentMapper.getFor(ComboComponent::class.java)
		val sceneTimeline: ComponentMapper<SceneTimelineComponent> = ComponentMapper.getFor(SceneTimelineComponent::class.java)
		val directionalSprite: ComponentMapper<DirectionalSpriteComponent> = ComponentMapper.getFor(DirectionalSpriteComponent::class.java)
		val water: ComponentMapper<WaterComponent> = ComponentMapper.getFor(WaterComponent::class.java)
		val event: ComponentMapper<EventComponent> = ComponentMapper.getFor(EventComponent::class.java)
		val trailing: ComponentMapper<TrailingEntityComponent> = ComponentMapper.getFor(TrailingEntityComponent::class.java)
	}
}

class EntityLoader()
{
	companion object
	{
		val sharedRenderableMap = ObjectMap<Int, Renderable>()

		val files: ObjectMap<String, FileHandle> by lazy { loadFiles() }

		private fun loadFiles(): ObjectMap<String, FileHandle>
		{
			val rootPath = "Entities"
			var root = Gdx.files.internal(rootPath)
			if (!root.exists()) root = Gdx.files.absolute(rootPath)

			val out = ObjectMap<String, FileHandle>()

			for (f in root.list())
			{
				out[f.nameWithoutExtension().toUpperCase()] = f
			}

			return out
		}

		@JvmStatic fun load(path: String): Entity
		{
			val xml = XmlReader().parse(files[path.toUpperCase()])
			val entity = load(xml)

			if (entity.name() == null) entity.add(NameComponent(path))

			return entity
		}

		@JvmStatic fun load(xml: XmlReader.Element): Entity
		{
			val entity = if (xml.get("Extends", null) != null) load(xml.get("Extends")) else Entity()

			if (xml.getBoolean("IsPlayer", false))
			{
				val name = entity.name() ?: NameComponent("player")
				name.isPlayer = true
				entity.add(name)
			}

			val componentsEl = xml.getChildByName("Components") ?: return entity

			val ai = componentsEl.getChildByName("AI")?.get("AI", null)
			if (ai != null) entity.add(TaskComponent(ai))

			val posEl = componentsEl.getChildByName("Position")
			if (posEl != null)
			{
				val pos = entity.pos() ?: PositionComponent()
				entity.add(pos)

				val slot = posEl.get("Slot", null)
				if (slot != null) pos.slot = SpaceSlot.valueOf(slot.toUpperCase())

				val size = posEl.getInt("Size", -1)
				if (size != -1)
				{
					pos.size = size

					val renderable = entity.renderable()
					if (renderable != null)
					{
						renderable.renderable.size[0] = size
						renderable.renderable.size[1] = size
					}

					val directional = entity.directionalSprite()
					if (directional != null)
					{
						directional.directionalSprite.size = size
					}

					val additional = entity.additionalRenderable()
					if (additional != null)
					{
						for (renderable in additional.below.values())
						{
							renderable.size[0] = size
							renderable.size[1] = size
						}

						for (renderable in additional.above.values())
						{
							renderable.size[0] = size
							renderable.size[1] = size
						}
					}
				}
			}

			val renderableCompEl = componentsEl.getChildByName("Renderable")
			if (renderableCompEl != null)
			{
				val renderableEl = renderableCompEl.getChildByName("Renderable")

				fun loadRenderable(): Renderable
				{
					val renderable = when (renderableEl.getAttribute("meta:RefKey"))
					{
						"Sprite" -> AssetManager.loadSprite(renderableEl)
						"TilingSprite" -> AssetManager.loadTilingSprite(renderableEl)
						"ParticleEffect" -> AssetManager.loadParticleEffect(renderableEl)
						else -> throw Exception("Unknown renderable type '" + renderableEl.getAttribute("meta:RefKey") + "'!")
					}

					val pos = entity.pos()
					if (pos != null)
					{
						renderable.size[0] = pos.size
						renderable.size[1] = pos.size
					}

					return renderable
				}

				if (renderableCompEl.getBoolean("IsShared", false))
				{
					val key = renderableCompEl.toString().hashCode()
					if (!sharedRenderableMap.containsKey(key))
					{
						sharedRenderableMap[key] = loadRenderable()
					}

					entity.add(RenderableComponent(sharedRenderableMap[key]))
				}
				else
				{
					val renderable = loadRenderable()

					entity.add(RenderableComponent(renderable))
				}
			}

			val directionalSprite = componentsEl.getChildByName("DirectionalSprite")
			if (directionalSprite != null) entity.add(DirectionalSpriteComponent(AssetManager.loadDirectionalSprite(directionalSprite, entity.pos()?.size ?: 1)))

			val light = componentsEl.getChildByName("Light")
			if (light != null) entity.add(LightComponent(AssetManager.loadColour(light.getChildByName("Colour")), light.getFloat("Distance")))

			val occludes = componentsEl.getChildByName("Occludes") != null
			if (occludes) entity.add(OccluderComponent())

			val statsEl = componentsEl.getChildByName("Statistics")
			if (statsEl != null)
			{
				val stats = entity.stats() ?: StatisticsComponent()

				stats.factions.addAll(statsEl.get("Faction").split(",").toGdxArray())
				stats.maxHP += statsEl.getInt("HP")
				stats.maxStamina += statsEl.getInt("Stamina")
				stats.sight += statsEl.getInt("Sight")

				entity.add(stats)
			}

			val comboEl = componentsEl.getChildByName("Combo")
			if (comboEl != null)
			{
				val combo = ComboComponent(ComboTree.load(comboEl.get("ComboTree")))
				entity.add(combo)
			}

			val waterEl = componentsEl.getChildByName("Water")
			if (waterEl != null)
			{
				val water = WaterComponent()
				water.depth = waterEl.getFloat("Depth", 0.3f)

				entity.add(water)
			}

			val additionalEl = componentsEl.getChildByName("AdditionalRenderables")
			if (additionalEl != null)
			{
				val additional = AdditionalRenderableComponent()

				val pos = entity.pos()

				val belowEls = additionalEl.getChildByName("Below")
				if (belowEls != null)
				{
					for (el in belowEls.children())
					{
						val key = el.get("Key")
						val renderable = AssetManager.loadRenderable(el.getChildByName("Renderable"))

						if (pos != null)
						{
							renderable.size[0] = pos.size
							renderable.size[1] = pos.size
						}

						additional.below[key] = renderable
					}
				}

				val aboveEls = additionalEl.getChildByName("Above")
				if (aboveEls != null)
				{
					for (el in aboveEls.children())
					{
						val key = el.get("Key")
						val renderable = AssetManager.loadRenderable(el.getChildByName("Renderable"))

						if (pos != null)
						{
							renderable.size[0] = pos.size
							renderable.size[1] = pos.size
						}

						additional.above[key] = renderable
					}
				}
			}

			val trailingEl = componentsEl.getChildByName("Trailing")
			if (trailingEl != null)
			{
				val trailing = TrailingEntityComponent()
				trailing.collapses = trailingEl.getBoolean("Collapses", true)
				entity.add(trailing)
				trailing.entities.add(entity)

				val renderablesEl = trailingEl.getChildByName("Renderables")
				for (el in renderablesEl.children())
				{
					val renderable = AssetManager.loadRenderable(el)
					val trailEntity = Entity()
					trailEntity.add(RenderableComponent(renderable))
					trailEntity.add(trailing)
					if (entity.stats() != null) trailEntity.add(entity.stats())
					trailEntity.add(PositionComponent())
					trailEntity.pos().slot = entity.pos().slot

					trailing.entities.add(trailEntity)
				}
			}

			return entity
		}
	}
}

fun Entity.isAllies(other: Entity): Boolean { return if (this.stats() != null && other.stats() != null) this.stats().factions.isAllies(other.stats().factions) else false }
