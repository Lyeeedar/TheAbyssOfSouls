package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.SceneTimelineComponent
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray

class ActionScene : AbstractAction()
{
	val hitPoints = Array<Point>()
	lateinit var scene: SceneTimeline

	override fun evaluate(entity: Entity): ExecutionState
	{
		val tile = entity.tile() ?: return ExecutionState.FAILED

		val hitTiles = Array<Tile>()
		for (point in hitPoints)
		{
			val t = tile.level.getTile(tile, point) ?: continue
			hitTiles.add(t)
		}

		val timeline = scene.copy()
		timeline.sourceTile = tile
		timeline.destinationTiles.addAll(hitTiles)

		for (t in hitTiles)
		{
			t.timelines.add(timeline)
		}

		val timelineEntity = Entity()
		val component = SceneTimelineComponent(timeline)
		timelineEntity.add(component)

		Global.engine.addEntity(timelineEntity)

		return ExecutionState.COMPLETED
	}

	override fun parse(xml: XmlReader.Element)
	{
		scene = SceneTimeline.load(xml.getChildByName("SceneTimeline"))
		hitPoints.addAll(xml.getChildByName("HitPoints").toHitPointArray())
	}

	override fun cancel(entity: Entity)
	{

	}
}
