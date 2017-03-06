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
	private val processedTimelines = Array<SceneTimeline>()
	
	override fun doUpdate(deltaTime: Float)
	{
		if (Global.interaction != null) return

		processedTimelines.clear()
		
		for (entity in entities)
		{
			processEntity(entity, deltaTime)
		}
	}

	fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val timeline = entity!!.sceneTimeline() ?: return
		if (processedTimelines.contains(timeline)) return
		
		if (timeline.isShared)
		{
			timeline.sceneTimeline.destinationTiles.clear()
			
			for (entity in timeline.sharingEntities)
			{
				val tile = entity.tile()!!
				
				for (point in timeline.hitPoints)
				{
					val t = tile.level.getTile(tile, point) ?: continue
					if (!timeline.sceneTimeline.destinationTiles.contains(t))
					{
						timeline.sceneTimeline.destinationTiles.add(t)
					}
				}
			}
			
			processedTimelines.add(timeline)
		}
		else
		{
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
