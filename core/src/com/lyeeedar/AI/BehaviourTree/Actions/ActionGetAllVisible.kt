package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.ShadowCastComponent
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionGetAllVisible(): AbstractAction()
{
	enum class Type
	{
		TILES, ALLIES, ENEMIES
	}

	var type: Type = Type.TILES
	lateinit var key: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val pos = Mappers.position.get(entity)
		val tile = pos.position as? Tile ?: return ExecutionState.FAILED
		val stats = Mappers.stats.get(entity)
		var cache = Mappers.shadow.get(entity)

		if (cache == null)
		{
			cache = ShadowCastComponent()

			cache.cache.getShadowCast(tile.level.grid, tile.x, tile.y, stats.stats.get(Enums.Statistic.SIGHT).toInt(), entity)

			entity.add(cache)
		}

		var points = cache.cache.currentShadowCast

		if (type == Type.ALLIES)
		{
			var temp = com.badlogic.gdx.utils.Array<Entity>()
			for (point in points)
			{
				for (e in tile.contents)
				{
					val estats = Mappers.stats.get(e) ?: continue

					if (estats.factions.intersect(stats.factions).size > 0)
					{
						temp.add(e)
					}
				}
			}

			parent.setData( key, temp );
			state = if(temp.size > 0) ExecutionState.COMPLETED else ExecutionState.FAILED;
		}
		else if (type == Type.ENEMIES)
		{
			var temp = com.badlogic.gdx.utils.Array<Entity>()
			for (point in points)
			{
				for (e in tile.contents)
				{
					val estats = Mappers.stats.get(e) ?: continue

					if (estats.factions.intersect(stats.factions).size == 0)
					{
						temp.add(e)
					}
				}
			}

			parent.setData( key, temp );
			state = if(temp.size > 0) ExecutionState.COMPLETED else ExecutionState.FAILED;
		}
		else
		{
			parent.setData( key, points );
			state = if(points.size > 0) ExecutionState.COMPLETED else ExecutionState.FAILED;
		}


		return state
	}

	override fun parse(xml: XmlReader.Element) {
		key = xml.getAttribute("Key")
		if (xml.getAttribute("Type", null) != null)
		{
			type = Type.valueOf(xml.getAttribute("Type").toUpperCase())
		}
	}

	override fun cancel() {

	}

}