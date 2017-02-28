package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.UI.DebugConsole
import com.lyeeedar.UI.IDebugCommandProvider
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray

class SceneTimelineComponent() : AbstractComponent(), IDebugCommandProvider
{
	lateinit var sceneTimeline: SceneTimeline
	val hitPoints = Array<Point>()

	constructor(sceneTimeline: SceneTimeline) : this()
	{
		this.sceneTimeline = sceneTimeline
	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		sceneTimeline = SceneTimeline.load(xml.getChildByName("SceneTimeline"))
		sceneTimeline.loop = xml.getBoolean("Loop", false)

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
				console.write("Blocked (" + blocker.blockCount + ")")
			}

			return true
		})
	}

	override fun detachCommands()
	{
		DebugConsole.unregister("SceneState")
	}
}