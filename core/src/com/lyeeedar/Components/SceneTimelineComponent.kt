package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray
import ktx.collections.set

class SceneTimelineComponent() : AbstractComponent(), IDebugCommandProvider
{
	lateinit var sceneTimeline: SceneTimeline
	val hitPoints = Array<Point>()

	var isShared = false

	constructor(sceneTimeline: SceneTimeline) : this()
	{
		this.sceneTimeline = sceneTimeline
	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		val timelinexml = xml.getChildByName("SceneTimeline")

		isShared = xml.getBoolean("IsShared", false)
		if (isShared)
		{
			val key = xml.toString().hashCode()

			synchronized(sharedTimelines)
			{
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

	companion object
	{
		val sharedTimelines = ObjectMap<Int, SceneTimeline>()
	}
}
