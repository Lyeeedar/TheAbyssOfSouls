package com.lyeeedar.Components

import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.children
import ktx.collections.set

fun Entity.pos() = Mappers.position.get(this)
fun Entity.tile() = Mappers.position.get(this)?.position as? Tile
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
fun Entity.dialogue() = Mappers.dialogue.get(this)
fun Entity.interaction() = Mappers.interaction.get(this)
fun Entity.pit() = Mappers.pit.get(this)
fun Entity.occludes() = Mappers.occludes.get(this)
fun Entity.pickup() = Mappers.pickup.get(this)
fun Entity.metaregion() = Mappers.metaregion.get(this)
fun Entity.loaddata() = Mappers.loaddata.get(this)
fun Entity.drop() = Mappers.drop.get(this)
fun Entity.sin() = Mappers.sin.get(this)

fun <T: AbstractComponent> Entity.hasComponent(c: Class<T>) = this.getComponent(c) != null

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
		val occludes: ComponentMapper<OccludesComponent> = ComponentMapper.getFor(OccludesComponent::class.java)
		val stats: ComponentMapper<StatisticsComponent> = ComponentMapper.getFor(StatisticsComponent::class.java)
		val shadow: ComponentMapper<ShadowCastComponent> = ComponentMapper.getFor(ShadowCastComponent::class.java)
		val combo: ComponentMapper<ComboComponent> = ComponentMapper.getFor(ComboComponent::class.java)
		val sceneTimeline: ComponentMapper<SceneTimelineComponent> = ComponentMapper.getFor(SceneTimelineComponent::class.java)
		val directionalSprite: ComponentMapper<DirectionalSpriteComponent> = ComponentMapper.getFor(DirectionalSpriteComponent::class.java)
		val water: ComponentMapper<WaterComponent> = ComponentMapper.getFor(WaterComponent::class.java)
		val event: ComponentMapper<EventComponent> = ComponentMapper.getFor(EventComponent::class.java)
		val trailing: ComponentMapper<TrailingEntityComponent> = ComponentMapper.getFor(TrailingEntityComponent::class.java)
		val dialogue: ComponentMapper<DialogueComponent> = ComponentMapper.getFor(DialogueComponent::class.java)
		var interaction: ComponentMapper<InteractionComponent> = ComponentMapper.getFor(InteractionComponent::class.java)
		val pit: ComponentMapper<PitComponent> = ComponentMapper.getFor(PitComponent::class.java)
		val pickup: ComponentMapper<PickupComponent> = ComponentMapper.getFor(PickupComponent::class.java)
		val metaregion: ComponentMapper<MetaRegionComponent> = ComponentMapper.getFor(MetaRegionComponent::class.java)
		val loaddata: ComponentMapper<LoadDataComponent> = ComponentMapper.getFor(LoadDataComponent::class.java)
		val drop: ComponentMapper<DropComponent> = ComponentMapper.getFor(DropComponent::class.java)
		val sin: ComponentMapper<SinComponent> = ComponentMapper.getFor(SinComponent::class.java)
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

			if (entity.name() == null)
			{
				val name = NameComponent(path)
				name.fromLoad = true
				entity.add(name)
			}

			return entity
		}

		@JvmStatic fun load(xml: XmlReader.Element): Entity
		{
			val entity = if (xml.get("Extends", null) != null) load(xml.get("Extends")) else Entity()

			entity.add(LoadDataComponent(xml))

			val componentsEl = xml.getChildByName("Components") ?: return entity

			for (componentEl in componentsEl.children())
			{
				val component = when(componentEl.name.toUpperCase())
				{
					"ADDITIONALRENDERABLES" -> AdditionalRenderableComponent()
					"COMBO" -> ComboComponent()
					"DIRECTIONALSPRITE" -> DirectionalSpriteComponent()
					"DROP" -> DropComponent()
					"INTERACTION" -> InteractionComponent()
					"LIGHT" -> LightComponent()
					"METAREGION" -> MetaRegionComponent()
					"NAME" -> NameComponent()
					"OCCLUDES" -> OccludesComponent()
					"POSITION" -> PositionComponent()
					"PICKUP" -> PickupComponent()
					"PIT" -> PitComponent()
					"RENDERABLE" -> RenderableComponent()
					"SCENETIMELINE" -> SceneTimelineComponent()
					"STATISTICS" -> StatisticsComponent()
					"AI" -> TaskComponent()
					"SIN" -> SinComponent()
					"TRAILING" -> TrailingEntityComponent()
					"WATER" -> WaterComponent()

					else -> throw Exception("Unknown component type '" + componentEl.name + "'!")
				}

				component.fromLoad = true
				component.parse(componentEl, entity)
				entity.add(component)
			}

			if (xml.getBoolean("IsPlayer", false))
			{
				val name = entity.name() ?: NameComponent("player")
				name.isPlayer = true
				name.fromLoad = true
				entity.add(name)
			}

			return entity
		}

		fun getSlot(xml: XmlReader.Element): SpaceSlot
		{
			var slot = SpaceSlot.ENTITY

			val extends = xml.get("Extends", null)
			if (extends != null)
			{
				val extendsxml = XmlReader().parse(files[extends.toUpperCase()])
				slot = getSlot(extendsxml)
			}

			val componentsEl = xml.getChildByName("Components")
			if (componentsEl != null)
			{
				val positionEl = componentsEl.getChildByName("Position")
				if (positionEl != null)
				{
					slot = SpaceSlot.valueOf(positionEl.get("SpaceSlot", "Entity").toUpperCase())
				}
			}

			return slot
		}
	}
}

fun Entity.isAllies(other: Entity): Boolean
{
	if (this.stats() == null || other.stats() == null) return false

	if (this.stats().factions.size == 0 || other.stats().factions.size == 0) return false

	return this.stats().factions.any { other.stats().factions.contains(it) }
}

fun Entity.isEnemies(other: Entity): Boolean
{
	if (this.stats() == null || other.stats() == null) return false

	if (this.stats().factions.size == 0 || other.stats().factions.size == 0) return false

	return !this.stats().factions.any { other.stats().factions.contains(it) }
}