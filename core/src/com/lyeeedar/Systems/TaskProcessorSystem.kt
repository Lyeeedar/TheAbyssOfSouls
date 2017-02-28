package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Components.*
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.Util.Event0Arg

/**
 * Created by Philip on 20-Mar-16.
 */

class TaskProcessorSystem(): AbstractSystem(Family.all(TaskComponent::class.java).get())
{
	lateinit var renderables: ImmutableArray<Entity>
	lateinit var timelines: ImmutableArray<Entity>

	val onTurnEvent = Event0Arg()

	var lastState = "---"
	var printTasks = false

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

		DebugConsole.register("TaskSystemLastState", "", fun (args, console): Boolean {
			console.write(lastState)

			return true
		})

		DebugConsole.register("PrintTasks", "", fun (args, console): Boolean {
			if (args[0] == "true" || args[0] == "false") printTasks = args[0] == "true"
			else
			{
				return false
			}

			return true
		})

		DebugConsole.register("god", "", fun (args, console): Boolean {
			if (args[0] == "true" || args[0] == "false") level!!.player.stats().godMode = args[0] == "true"
			else
			{
				return false
			}

			return true
		})
	}

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(TaskComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		renderables = engine?.getEntitiesFor(Family.all(RenderableComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		timelines = engine?.getEntitiesFor(Family.all(SceneTimelineComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun doUpdate(deltaTime: Float)
	{
		val hasEffects = renderables.any { it.renderable()!!.renderable.animation != null }
		var hasTimelines = false

		if (!hasEffects)
		{
			hasTimelines = timelines.any{ !it.sceneTimeline()!!.sceneTimeline.loop && it.sceneTimeline()!!.sceneTimeline.isRunning }
		}

		if (!hasEffects && !hasTimelines)
		{
			lastState = "Waiting on player"

			// process player
			val player = level!!.player

			val tookTurn = processEntity(player)

			if (tookTurn)
			{
				doTurn()
			}
		}
		else if (hasEffects)
		{
			lastState = "Waiting on effects"
		}
		else if (hasTimelines)
		{
			lastState = "Waiting on timelines"
		}
	}

	fun doTurn()
	{
		val player = level!!.player

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

				processEntity(entity)
			}
		}

		onTurnEvent()

		for (system in systemList)
		{
			(engine.getSystem(system.java) as AbstractSystem).onTurn()
		}
	}

	fun processEntity(e: Entity): Boolean
	{
		val task = e.task() ?: return false

		if (e.stats()?.hp ?: 1f <= 0) return false

		val trailing = e.trailing()
		if (trailing != null && !trailing.initialised)
		{
			trailing.updatePos(e.tile()!!)
			trailing.initialised = true
		}

		if (task.tasks.size == 0)
		{
			task.ai.update(e)
		}

		if (task.tasks.size > 0)
		{
			e.event().onTurn()

			val t = task.tasks.removeIndex(0)

			if (printTasks)
			{
				println("Entity '" + e.name().name + "' doing task '" + t.javaClass.simpleName + "'")
			}

			t.execute(e)

			e.pos().turnsOnTile++

			e.trailing()?.updatePos(e.tile()!!)

			return true
		}
		else
		{
			val trailing = e.trailing()
			if (trailing != null && trailing.collapses)
			{
				trailing.updatePos(e.tile()!!)
			}
		}

		return false
	}
}
