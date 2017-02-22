package com.lyeeedar.Systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile

class SceneTimelineSystem(): IteratingSystem(Family.all(SceneTimelineComponent::class.java).get(), systemList.indexOf(SceneTimelineSystem::class))
{
	var level: Level? = null
		get() = field
		set(value)
		{
			field = value
		}

	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val timeline = entity!!.sceneTimeline() ?: return

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