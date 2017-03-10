package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.SceneTimeline.BlockerAction
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray
import ktx.collections.set

class SceneTimelineComponent() : AbstractComponent(), IDebugCommandProvider
{
	lateinit var sceneTimeline: SceneTimeline
	val hitPoints = Array<Point>()

	var isShared = false
	var canRemove = true

	constructor(sceneTimeline: SceneTimeline) : this()
	{
		this.sceneTimeline = sceneTimeline
	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		canRemove = false

		val timelinexml = xml.getChildByName("SceneTimeline")

		isShared = xml.getBoolean("IsShared", false)
		if (isShared)
		{
			synchronized(sharedTimelines)
			{
				val key = xml.toString().hashCode()

				if (sharedTimelines.containsKey(key))
				{
					sceneTimeline = sharedTimelines[key]
				}
				else
				{
					sceneTimeline = SceneTimeline.load(timelinexml)
					sceneTimeline.loop = xml.getBoolean("Loop", false)
					sceneTimeline.parentEntity = entity

					sharedTimelines[key] = sceneTimeline
				}

				sceneTimeline.sharingEntities.add(entity)
			}
		}
		else
		{
			sceneTimeline = SceneTimeline.load(timelinexml)
			sceneTimeline.loop = xml.getBoolean("Loop", false)

			sceneTimeline.parentEntity = entity
		}

		val hitPointsEl = xml.getChildByName("HitPoints")
		if (hitPointsEl != null) hitPoints.addAll(hitPointsEl.toHitPointArray())
		else hitPoints.add(Point(0, 0))
	}

	override fun attachCommands()
	{
		DebugConsole.register("SceneState", "", fun(args, console): Boolean {
			if (sceneTimeline.isRunning) console.write("Running")
			else if (sceneTimeline.isComplete) console.write("Complete")
			else
			{
				val blocker = sceneTimeline.blocker!!
				console.write("Blocked ($blocker)")
			}

			return true
		})
	}

	override fun detachCommands()
	{
		DebugConsole.unregister("SceneState")
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		output.writeFloat(sceneTimeline.progression)

		for (timeline in sceneTimeline.timelines)
		{
			for (action in timeline.actions)
			{
				if (action.isExited)
				{
					output.writeInt(2, true)
				}
				else if (action.isEntered)
				{
					output.writeInt(1, true)

					if (action is BlockerAction)
					{
						output.writeInt(action.blockCount, true)
					}
				}
				else
				{
					output.writeInt(0, true)
				}
			}
		}
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		sceneTimeline.progression = input.readFloat()

		for (timeline in sceneTimeline.timelines)
		{
			for (action in timeline.actions)
			{
				val state = input.readInt(true)

				if (state == 2)
				{
					action.isEntered = true
					action.isExited = true
				}
				else if (state == 1)
				{
					Future.call({action.enter()}, 0f)

					if (action is BlockerAction)
					{
						action.blockCount = input.readInt(true)
					}
				}
				else
				{

				}
			}
		}
	}

	companion object
	{
		val sharedTimelines = ObjectMap<Int, SceneTimeline>()
	}
}
