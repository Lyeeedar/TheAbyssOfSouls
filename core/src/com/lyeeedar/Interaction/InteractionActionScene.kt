package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.SceneTimelineComponent
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray

class InteractionActionScene : AbstractInteractionAction()
{
	lateinit var sceneTimeline: SceneTimeline
	val hitPoints = Array<Point>()

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		val parentTile = parent.tile()!!

		val timeline = sceneTimeline.copy()
		timeline.parentEntity = parent
		timeline.sourceTile = parentTile

		for (point in hitPoints)
		{
			val dx = point.x
			val dy = point.y

			val tile = parentTile.level.getTile(parentTile, dx, dy)
			if (tile != null) timeline.destinationTiles.add(tile)
		}

		val timelineEntity = Entity()
		val component = SceneTimelineComponent(timeline)
		timelineEntity.add(component)

		Global.engine.addEntity(timelineEntity)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		val timelinexml = xml.getChildByName("SceneTimeline")

		sceneTimeline = SceneTimeline.load(timelinexml)
		sceneTimeline.loop = xml.getBoolean("Loop", false)

		val hitPointsEl = xml.getChildByName("HitPoints")
		if (hitPointsEl != null) hitPoints.addAll(hitPointsEl.toHitPointArray())
		else hitPoints.add(Point(0, 0))
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}

}
