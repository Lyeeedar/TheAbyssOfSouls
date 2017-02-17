package com.lyeeedar.Components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import ktx.collections.set
import ktx.collections.toGdxArray

fun Entity.pos() = Mappers.position.get(this)
fun Entity.tile() = Mappers.position.get(this).position as? Tile
fun Entity.stats() = Mappers.stats.get(this)
fun Entity.renderable() = Mappers.renderable.get(this)
fun Entity.renderOffset() = this.renderable()?.renderable?.animation?.renderOffset()
fun Entity.task() = Mappers.task.get(this)
fun Entity.combo() = Mappers.combo.get(this)
fun Entity.sceneTimeline() = Mappers.sceneTimeline.get(this)
fun Entity.shadow() = Mappers.shadow.get(this)
fun Entity.directionalSprite() = Mappers.directionalSprite.get(this)

class Mappers
{
	companion object
	{
		val position: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
		val renderable: ComponentMapper<RenderableComponent> = ComponentMapper.getFor(RenderableComponent::class.java)
		val task: ComponentMapper<TaskComponent> = ComponentMapper.getFor(TaskComponent::class.java)
		val light: ComponentMapper<LightComponent> = ComponentMapper.getFor(LightComponent::class.java)
		val occluder: ComponentMapper<OccluderComponent> = ComponentMapper.getFor(OccluderComponent::class.java)
		val impassable: ComponentMapper<ImpassableComponent> = ComponentMapper.getFor(ImpassableComponent::class.java)
		val stats: ComponentMapper<StatisticsComponent> = ComponentMapper.getFor(StatisticsComponent::class.java)
		val shadow: ComponentMapper<ShadowCastComponent> = ComponentMapper.getFor(ShadowCastComponent::class.java)
		val combo: ComponentMapper<ComboComponent> = ComponentMapper.getFor(ComboComponent::class.java)
		val sceneTimeline: ComponentMapper<SceneTimelineComponent> = ComponentMapper.getFor(SceneTimelineComponent::class.java)
		val directionalSprite: ComponentMapper<DirectionalSpriteComponent> = ComponentMapper.getFor(DirectionalSpriteComponent::class.java)
	}
}

class EntityLoader()
{
	companion object
	{
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
			return load(xml)
		}

		@JvmStatic fun load(xml: XmlReader.Element): Entity
		{
			val entity = if (xml.get("Extends", null) != null) load(xml.get("Extends")) else Entity()

			val ai = xml.getChildByName("AI")?.get("AI", null)
			if (ai != null) entity.add(TaskComponent(ai))

			val posEl = xml.getChildByName("Position")
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
				}
			}

			val renderableCompEl = xml.getChildByName("Renderable")
			if (renderableCompEl != null)
			{
				for (renderableEl in renderableCompEl.children())
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

					entity.add(RenderableComponent(renderable))
				}
			}

			val directionalSprite = xml.getChildByName("DirectionalSprite")
			if (directionalSprite != null) entity.add(DirectionalSpriteComponent(AssetManager.loadDirectionalSprite(directionalSprite, entity.pos()?.size ?: 1)))

			val light = xml.getChildByName("Light")
			if (light != null) entity.add(LightComponent(AssetManager.loadColour(light.getChildByName("Colour")), light.getFloat("Distance")))

			val occludes = xml.getChildByName("Occludes") != null
			if (occludes) entity.add(OccluderComponent())

			val statsEl = xml.getChildByName("Statistics")
			if (statsEl != null)
			{
				val stats = entity.stats() ?: StatisticsComponent()

				stats.factions.addAll(statsEl.get("Faction").split(",").toGdxArray())
				stats.maxHP += statsEl.getInt("HP")
				stats.maxStamina += statsEl.getInt("Stamina")

				entity.add(stats)
			}

			val comboEl = xml.getChildByName("Combo")
			if (comboEl != null)
			{
				val combo = ComboComponent()

				val trees = ComboTree.load(comboEl.get("ComboTree"))
				for (tree in trees)
				{
					combo.combos.add(tree)
				}

				entity.add(combo)
			}

			return entity
		}
	}
}

fun Entity.isAllies(other: Entity): Boolean { return if (this.stats() != null && other.stats() != null) this.stats().factions.isAllies(other.stats().factions) else false }
