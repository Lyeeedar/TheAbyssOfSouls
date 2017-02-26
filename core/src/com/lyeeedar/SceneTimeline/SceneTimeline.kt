package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Systems.task
import com.lyeeedar.Util.children

class SceneTimeline
{
	var facing: Direction = Direction.CENTRE

	var sourceTile: Tile? = null
	val destinationTiles = Array<Tile>()

	private val timelines = Array<Timeline>()

	private var progression: Float = 0f

	val isRunning: Boolean
		get()
		{
			return timelines.any { (it.getExact(progression) as? BlockerAction)?.isBlocked != true }
		}

	val isComplete: Boolean
		get() = progression > duration

	val duration: Float by lazy { timelines.map { it.duration }.max()!! }

	fun update(delta: Float)
	{
		if (!isRunning || isComplete) return

		val oldTime = progression
		var newTime = progression + delta

		for (timeline in timelines)
		{
			val actions = timeline.get(oldTime, newTime)
			for (action in actions)
			{
				if (!action.isEntered)
				{
					if (action is BlockerAction)
					{
						newTime = action.startTime
						action.enter()
					}
				}
			}
		}

		if (isRunning)
		{
			for (timeline in timelines)
			{
				val actions = timeline.get(oldTime, newTime)
				for (action in actions)
				{
					if (!action.isEntered)
					{
						action.enter()
						action.isEntered = true
					}

					if (action.endTime <= newTime)
					{
						action.exit()
					}
				}
			}
		}

		progression = newTime
	}

	fun copy(): SceneTimeline
	{
		val scene = SceneTimeline()
		for (timeline in timelines)
		{
			scene.timelines.add(timeline.copy(scene))
		}
		return scene
	}

	companion object
	{
		fun load(xml: XmlReader.Element): SceneTimeline
		{
			val scene = SceneTimeline()

			for (el in xml.children())
			{
				val timeline = Timeline.load(el, scene)
				scene.timelines.add(timeline)
			}

			return scene
		}
	}
}

class Timeline
{
	val actions = Array<AbstractTimelineAction>()

	val duration: Float by lazy { actions.last()?.endTime ?: 0f }

	fun getExact(time: Float): AbstractTimelineAction? = actions.firstOrNull{ it.startTime == time }
	fun get(time: Float): AbstractTimelineAction? = actions.firstOrNull{ it.startTime <= time }
	fun get(start: Float, end: Float): Iterable<AbstractTimelineAction> = actions.select { start <= it.endTime && end >= it.startTime }

	fun copy(parent: SceneTimeline): Timeline
	{
		val timeline = Timeline()
		for (action in actions)
		{
			timeline.actions.add(action.copy(parent))
		}
		return timeline
	}

	companion object
	{
		fun load(xml: XmlReader.Element, parent: SceneTimeline): Timeline
		{
			val timeline = Timeline()

			for (el in xml.children())
			{
				val action = AbstractTimelineAction.load(el)
				action.parent = parent
				timeline.actions.add(action)
			}

			return timeline
		}
	}
}

abstract class AbstractTimelineAction()
{
	lateinit var parent: SceneTimeline

	var isEntered: Boolean = false

	var startTime: Float = 0f
	var duration: Float = 0f

	val endTime: Float
		get() = startTime + duration

	abstract fun enter()
	abstract fun exit()
	abstract fun copy(parent: SceneTimeline): AbstractTimelineAction
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractTimelineAction
		{
			val action = get(xml.name)
			action.parse(xml)

			action.startTime = xml.getFloat("Time", 0f)
			action.duration = xml.getFloat("Duration", 0f)

			return action
		}

		private fun get(name: String): AbstractTimelineAction
		{
			val uname = name.toUpperCase()
			val instance = when(uname) {
				"BLOCKER" -> BlockerAction()

				"DESTINATIONRENDERABLE" -> DestinationRenderableAction()
				"MOVEMENTRENDERABLE" -> MovementRenderableAction()

				"DAMAGE" -> DamageAction()
				"SPAWN" -> SpawnAction()

				// ARGH everything broke
				else -> throw RuntimeException("Invalid scene timeline action type: $name")
			}

			return instance
		}
	}
}

class BlockerAction() : AbstractTimelineAction()
{
	var blockOnTurn = false
	var isBlocked = false

	override fun enter()
	{
		if (blockOnTurn)
		{
			Global.engine.task().onTurn += fun(): Boolean {
				exit()
				return true
			}
		}

		isBlocked = true
	}

	override fun exit()
	{
		isBlocked = true
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = BlockerAction()
		action.parent = parent
		action.blockOnTurn = blockOnTurn
		action.startTime = startTime
		action.duration = duration
		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		blockOnTurn = true
	}
}