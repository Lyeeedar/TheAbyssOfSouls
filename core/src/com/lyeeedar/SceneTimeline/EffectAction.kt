package com.lyeeedar.SceneTimeline

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.*
import com.lyeeedar.ElementType
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Colour

class DamageAction() : AbstractTimelineAction()
{
	var amount: Int = 1
	lateinit var element: ElementType
	var elementalConversion: Float = 0.0f

	override fun enter()
	{
		for (tile in parent.destinationTiles)
		{
			for (entity in tile.contents)
			{
				val stats = entity.stats() ?: continue
				stats.dealDamage(amount, element, elementalConversion)

				val sprite = entity.renderable()?.renderable as? Sprite ?: continue
				sprite.colourAnimation = BlinkAnimation.obtain().set(Colour(1f, 0.5f, 0.5f, 1f), sprite.colour, 0.15f, true)
			}
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = DamageAction()
		action.parent = parent

		action.startTime = startTime
		action.duration = duration

		action.amount = amount
		action.element = element
		action.elementalConversion = elementalConversion

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		amount = xml.getInt("Amount", 1)
		element = ElementType.valueOf(xml.get("Element", "None").toUpperCase())
		elementalConversion = xml.getFloat("ElementalConversion", 0.0f)
	}
}

class SpawnAction() : AbstractTimelineAction()
{
	lateinit var entityXml: XmlReader.Element
	var deleteOnExit = false

	val spawnedEntities = Array<Entity>()

	override fun enter()
	{
		for (tile in parent.destinationTiles)
		{
			val entity = EntityLoader.load(entityXml)

			if (!tile.contents.containsKey(entity.pos().slot) || tile.contents.containsKey(SpaceSlot.WALL))
			{
				entity.pos().tile = tile
				tile.contents[entity.pos().slot] = entity

				Global.engine.addEntity(entity)

				spawnedEntities.add(entity)
			}
			else
			{
				System.err.println("Tried to spawn entity '" + entity.name().name + "' in non-empty tile!")
			}
		}
	}

	override fun exit()
	{
		if (deleteOnExit)
		{
			for (entity in spawnedEntities)
			{
				entity.add(MarkedForDeletionComponent())
			}
		}
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = SpawnAction()
		out.parent = parent
		out.entityXml = entityXml
		out.deleteOnExit = deleteOnExit

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityXml = xml.getChildByName("Entity")
		deleteOnExit = xml.getBoolean("DeleteOnExit", false)
	}

}