package com.lyeeedar.Components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.children
import ktx.collections.set

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

			val componentsEl = xml.getChildByName("Components") ?: return entity

			for (componentEl in componentsEl.children())
			{
				val component = when(componentEl.name.toUpperCase())
				{
					"ADDITIONALRENDERABLES" -> AdditionalRenderableComponent()
					"COMBO" -> ComboComponent()
					"DIRECTIONALSPRITE" -> DirectionalSpriteComponent()
					"LIGHT" -> LightComponent()
					"NAME" -> NameComponent()
					"OCCLUDER" -> OccluderComponent()
					"POSITION" -> PositionComponent()
					"RENDERABLE" -> RenderableComponent()
					"SCENE" -> SceneTimelineComponent()
					"STATISTICS" -> StatisticsComponent()
					"AI" -> TaskComponent()
					"TRAILING" -> TrailingEntityComponent()
					"WATER" -> WaterComponent()

					else -> throw Exception("Unknown component type '" + componentEl.name + "'!")
				}

				component.parse(componentEl, entity)
				entity.add(component)
			}

			if (xml.getBoolean("IsPlayer", false))
			{
				val name = entity.name() ?: NameComponent("player")
				name.isPlayer = true
				entity.add(name)
			}

			return entity
		}
	}
}

fun Entity.isAllies(other: Entity): Boolean { return if (this.stats() != null && other.stats() != null) this.stats().factions.isAllies(other.stats().factions) else false }
