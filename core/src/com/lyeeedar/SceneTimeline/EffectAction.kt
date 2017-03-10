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
	var blockable = true

	var attackPower = 100

	override fun enter()
	{
		for (tile in parent.destinationTiles)
		{
			for (entity in tile.contents)
			{
				val stats = entity.stats() ?: continue
				stats.dealDamage((amount * attackPower) / 100.0f, element, elementalConversion, blockable)
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
		action.attackPower = attackPower
		action.blockable = blockable

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		amount = xml.getInt("Amount", 1)
		element = ElementType.valueOf(xml.get("Element", "None").toUpperCase())
		elementalConversion = xml.getFloat("ElementalConversion", 0.0f)
		blockable = xml.getBoolean("Blockable", true)
	}
}

class SpawnAction() : AbstractTimelineAction()
{
	lateinit var entityXml: XmlReader.Element
	lateinit var entitySlot: SpaceSlot
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
					if (!tile.contents.containsKey(entitySlot) && (ignoreWall || !tile.contents.containsKey(SpaceSlot.WALL)))
					{
						val entity = EntityLoader.load(entityXml)

						entity.pos().tile = tile
						tile.contents[entity.pos().slot] = entity

						synchronized(Global.engine)
						{
							Global.engine.addEntity(entity)

							spawnedEntities.add(entity)
						}
					}
					else if (!Global.release)
					{
						System.err.println("Tried to spawn entity in non-empty tile at slot '$entitySlot'!")
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
		out.entitySlot = entitySlot
		out.deleteOnExit = deleteOnExit
		out.ignoreWall = ignoreWall

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityXml = xml.getChildByName("Entity")
		deleteOnExit = xml.getBoolean("DeleteOnExit", false)
		ignoreWall = xml.getBoolean("IgnoreWall", false)

		entitySlot = EntityLoader.getSlot(entityXml)
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