package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.Level.Level
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Event0Arg

/**
 * Created by Philip on 20-Mar-16.
 */

class TaskProcessorSystem(): EntitySystem(systemList.indexOf(TaskProcessorSystem::class))
{
	lateinit var entities: ImmutableArray<Entity>
	lateinit var renderables: ImmutableArray<Entity>
	lateinit var timelines: ImmutableArray<Entity>
	val entitiesToBeProcessed: com.badlogic.gdx.utils.Array<Entity> = com.badlogic.gdx.utils.Array<Entity>(false, 32)

	val onTurn = Event0Arg()

	var level: Level? = null
		get() = field
		set(value)
		{
			field = value
		}

	var processDuration: Float = 0f

	init
	{
		DebugConsole.register("spawn", "spawn entity x,y to spawn an entity at the given x,y pos relative to the player", fun (args, console): Boolean {
			if (args.size != 2)
			{
				console.error("Invalid number of args!")
				return false
			}

			val entity = EntityLoader.load(args[0])

			val split = args[1].split(',')
			val offsetx = split[0].toInt()
			val offsety = split[1].toInt()

			val player = level!!.player
			val playerPos = player.tile()!!

			for (x in 0..entity.pos()!!.size-1)
			{
				for (y in 0..entity.pos()!!.size-1)
				{
					val t = playerPos.level.getTile(playerPos, offsetx + x, offsety + y)
					if (t == null)
					{
						console.error("Pos out of bounds!")
						return false
					}
					if (t.contents.containsKey(entity.pos()!!.slot))
					{
						console.error("Tile already full!")
						return false
					}
				}
			}

			val entityTile = playerPos.level.getTile(playerPos, offsetx, offsety)!!
			entity.pos().tile = entityTile

			for (x in 0..entity.pos()!!.size-1)
			{
				for (y in 0..entity.pos()!!.size - 1)
				{
					val t = playerPos.level.getTile(playerPos, offsetx + x, offsety + y)!!
					t.contents[entity.pos()!!.slot] = entity
				}
			}

			engine.addEntity(entity)

			return true
		})
	}

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(TaskComponent::class.java, StatisticsComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		renderables = engine?.getEntitiesFor(Family.all(RenderableComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		timelines = engine?.getEntitiesFor(Family.all(SceneTimelineComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		val start = System.nanoTime()

		var hasEffects = renderables.any { it.renderable()!!.renderable.animation != null }

		if (!hasEffects)
		{
			hasEffects = timelines.any{ it.sceneTimeline()!!.sceneTimeline.isRunning }
		}

		if (!hasEffects)
		{
			if (entitiesToBeProcessed.size == 0)
			{
				// process player
				val player = level!!.player

				processEntity(player)

				val playerTask = Mappers.task.get(player)
				if (playerTask.actionDelay < 0)
				{
					for (entity in entities)
					{
						if (entity != player)
						{
							val pos = entity.pos()
							if (pos != null)
							{
								// skip far away entities
								if (pos.position.taxiDist(player.tile()!!) > 100) continue
							}

							val task = Mappers.task.get(entity)
							task.actionDelay -= playerTask.actionDelay

							if (task.actionDelay >= 0)
							{
								entitiesToBeProcessed.add(entity)
							}
						}
					}

					playerTask.actionDelay = 0f

					onTurn()
				}
			}

			for (i in 0..entitiesToBeProcessed.size-1)
			{
				if (entitiesToBeProcessed.size > 0)
				{
					val entity = entitiesToBeProcessed[0]
					entitiesToBeProcessed.removeIndex(0)
					val finished = processEntity(entity)

					if (!finished) entitiesToBeProcessed.add(entity)
				}
			}
		}

		val end = System.nanoTime()
		val diff = (end - start) / 1000000000f

		processDuration = (processDuration + diff) / 2f
	}

	fun processEntityStats(e: Entity)
	{
		val stats = Mappers.stats.get(e)

		if (stats.hp == stats.maxHP)
		{
			stats.bonusHP = 0f
		}
		else if (stats.bonusHP > 0)
		{
			stats.hp += Math.max(stats.bonusHP, 10f)
		}

		if (!stats.staminaReduced && stats.stamina < stats.maxStamina)
		{
			stats.stamina += 10f
		}
	}

	fun processEntity(e: Entity): Boolean
	{
		val task = e.task() ?: return true
		val stats = e.stats() ?: return true

		if (stats.hp <= 0) return true

		stats.staminaReduced = false

		if (task.actionDelay >= 0)
		{
			if (task.tasks.size == 0)
			{
				task.ai.update(e)
			}

			if (task.tasks.size > 0)
			{
				val t = task.tasks.removeIndex(0)

				t.execute(e)
				task.actionDelay -= 1f

				e.pos().turnsOnTile++
				processEntityStats(e)
			}
			else
			{
				task.actionDelay = 0f
				return true
			}
		}

		return task.actionDelay < 0
	}
}
