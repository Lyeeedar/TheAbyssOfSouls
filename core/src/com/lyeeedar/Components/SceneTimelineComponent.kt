package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray

class SceneTimelineComponent() : AbstractComponent()
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
	}
}