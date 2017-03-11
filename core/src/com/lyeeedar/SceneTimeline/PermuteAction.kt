package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Vector3
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

		val mat = Matrix3()
		mat.setToRotation(parent.facing.angle)
		val vec = Vector3()

		val addedset = ObjectSet<Tile>()
		for (tile in current)
		{
			for (point in hitPoints)
			{
				vec.set(point.x.toFloat(), point.y.toFloat(), 0f)
				vec.mul(mat)

				val dx = Math.round(vec.x)
				val dy = Math.round(vec.y)

				val ntile = tile.level.getTile(tile, dx, dy) ?: continue
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