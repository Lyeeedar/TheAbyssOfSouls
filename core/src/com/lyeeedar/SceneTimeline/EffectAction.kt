package com.lyeeedar.SceneTimeline

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.Tasks.TaskInterrupt
import com.lyeeedar.Components.*
import com.lyeeedar.ElementType
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Random
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

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
	var ignoreWall = false

	val spawnedEntities = Array<Entity>()

	override fun enter()
	{
		runBlocking {
			val jobs = Array<Job>(parent.destinationTiles.size)

			for (tile in parent.destinationTiles)
			{
				if (tile == null) continue

				val job = launch(CommonPool)
				{
					val entity = EntityLoader.load(entityXml)

					if (!tile.contents.containsKey(entity.pos().slot) && (ignoreWall || !tile.contents.containsKey(SpaceSlot.WALL)))
					{
						entity.pos().tile = tile
						tile.contents[entity.pos().slot] = entity

						synchronized(Global.engine)
						{
							Global.engine.addEntity(entity)

							spawnedEntities.add(entity)
						}
					}
					else
					{
						System.err.println("Tried to spawn entity '" + entity.name().name + "' in non-empty tile!")
					}
				}
				jobs.add(job)
			}

			for (job in jobs) job.join()
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
		out.ignoreWall = ignoreWall

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityXml = xml.getChildByName("Entity")
		deleteOnExit = xml.getBoolean("DeleteOnExit", false)
		ignoreWall = xml.getBoolean("IgnoreWall", false)
	}

}

class StunAction() :  AbstractTimelineAction()
{
	var chance = 1f

	override fun enter()
	{
		for (tile in parent.destinationTiles)
		{
			for (entity in tile.contents)
			{
				val task = entity.task() ?: continue

				if (Random.random() <= chance)
				{
					task.tasks.clear()
					task.tasks.add(TaskInterrupt())
				}
			}
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = StunAction()
		action.parent = parent
		action.startTime = startTime
		action.duration = duration

		action.chance = chance

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		chance = xml.getFloat("Chance", 1f)
	}

}