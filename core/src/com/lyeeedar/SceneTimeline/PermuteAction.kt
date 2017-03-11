package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.toHitPointArray
import ktx.collections.toGdxArray

class PermuteAction : AbstractTimelineAction()
{
	val hitPoints = Array<Point>()

	override fun enter()
	{
		val current = parent.destinationTiles.toGdxArray()
		parent.destinationTiles.clear()

		val addedset = ObjectSet<Tile>()
		for (tile in current)
		{
			for (point in hitPoints)
			{
				val ntile = tile.level.getTile(tile, point) ?: continue
				if (!addedset.contains(ntile))
				{
					parent.destinationTiles.add(ntile)
					addedset.add(ntile)
				}
			}
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = PermuteAction()
		action.parent = parent

		action.startTime = startTime
		action.duration = duration

		action.hitPoints.addAll(hitPoints)

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		val hitPointsEl = xml.getChildByName("HitPoints")
		if (hitPointsEl != null) hitPoints.addAll(hitPointsEl.toHitPointArray())
		else hitPoints.add(Point(0, 0))
	}
}