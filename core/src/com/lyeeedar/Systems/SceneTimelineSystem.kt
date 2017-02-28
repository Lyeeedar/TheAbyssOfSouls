package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.lyeeedar.Components.SceneTimelineComponent
import com.lyeeedar.Components.sceneTimeline
import com.lyeeedar.Components.tile
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile

class SceneTimelineSystem(): AbstractSystem(Family.all(SceneTimelineComponent::class.java).get())
{
	override fun doUpdate(deltaTime: Float)
	{
		for (entity in entities)
		{
			processEntity(entity, deltaTime)
		}
	}

	fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val timeline = entity!!.sceneTimeline() ?: return

		if (timeline.hitPoints.size > 0 && entity.tile() != null)
		{
			val tile = entity.tile()!!
			timeline.sceneTimeline.sourceTile = tile
			timeline.sceneTimeline.destinationTiles.clear()

			for (point in timeline.hitPoints)
			{
				val t = tile.level.getTile(tile, point) ?: continue
				timeline.sceneTimeline.destinationTiles.add(t)
			}
		}

		var visible = timeline.sceneTimeline.sourceTile?.isVisible ?: false
		if (!visible) visible = timeline.sceneTimeline.destinationTiles.any(Tile::isVisible)

		if (visible)
		{
			timeline.sceneTimeline.update(deltaTime)
		}
		else
		{
			timeline.sceneTimeline.update(timeline.sceneTimeline.duration)
		}

		if (timeline.sceneTimeline.isComplete)
		{
			if (timeline.sceneTimeline.loop)
			{
				timeline.sceneTimeline.reset()
			}
			else
			{
				entity.remove(SceneTimelineComponent::class.java)

				if (entity.components.size() == 0)
				{
					Global.engine.removeEntity(entity)

					for (tile in timeline.sceneTimeline.destinationTiles)
					{
						tile.timelines.removeValue(timeline.sceneTimeline, true)
					}
				}
			}
		}
	}
}