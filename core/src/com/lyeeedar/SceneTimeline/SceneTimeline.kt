package com.lyeeedar.SceneTimeline

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.children

class SceneTimeline
{
	var loop = false
	var facing: Direction = Direction.CENTER

	val sharingEntities = Array<Entity>()
	var parentEntity: Entity? = null
	var sourceTile: Tile? = null
	val destinationTiles = Array<Tile>()

	val timelines = Array<Timeline>()

	var progression: Float = 0f

	val isRunning: Boolean
		get()
		{
			return blocker == null && !isComplete
		}

	var blocker: AbstractBlockerAction? = null

	val isComplete: Boolean
		get() = progression >= duration && timelines.all { it.actions.last().isExited }

	val duration: Float by lazy { timelines.map { it.duration }.max()!! }

	fun update(delta: Float)
	{
		if (isComplete) return

		val oldTime = progression
		var newTime = progression + delta

		blocker = null
		for (action in timelines.flatMap { it.get(oldTime, newTime) }.sortedBy { it.startTime })
		{
			if (action is AbstractBlockerAction)
			{
				if (!action.isEntered)
				{
					action.enter()
					action.isEntered = true
				}

				if (action.isBlocked)
				{
					newTime = action.startTime
					blocker = action
					break
				}
			}
		}

		for (timeline in timelines)
		{
			val actions = timeline.get(oldTime, newTime)
			for (action in actions)
			{
				if (!action.isEntered)
				{
					action.isEntered = true
					action.enter()
				}

				if (action.endTime <= newTime && !action.isExited && action !is AbstractBlockerAction)
				{
					action.isExited = true
					action.exit()
				}
			}
		}

		progression = newTime
	}

	fun reset()
	{
		progression = 0f
		for (timeline in timelines)
		{
			for (action in timeline.actions)
			{
				action.isEntered = false
				action.isExited = false
			}
		}
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

	val duration: Float by lazy { actions.map { it.endTime }.max() ?: 0f }

	fun getExact(time: Float): AbstractTimelineAction? = actions.firstOrNull{ it.startTime == time }
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
	var isExited: Boolean = false

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
				"PROXIMITY" -> ProximityAction()
				"SIGNAL" -> SignalAction()

				"SOURCERENDERABLE" -> SourceRenderableAction()
				"SOURCEANIMATION" -> SourceAnimationAction()
				"DESTINATIONRENDERABLE" -> DestinationRenderableAction()
				"MOVEMENTRENDERABLE" -> MovementRenderableAction()
				"SCREENSHAKE" -> ScreenShakeAction()

				"MOVESOURCE" -> MoveSourceAction()
				"PULL" -> PullAction()
				"KNOCKBACK" -> KnockbackAction()

				"DAMAGE" -> DamageAction()
				"SPAWN" -> SpawnAction()
				"STUN" -> StunAction()

				"SPEECH" -> SpeechAction()
				"INTERACTION" -> InteractionAction()

				"ADDCOMPONENT" -> AddComponentAction()
				"REMOVECOMPONENT" -> RemoveComponentAction()

				// ARGH everything broke
				else -> throw RuntimeException("Invalid scene timeline action type: $name")
			}

			return instance
		}
	}
}