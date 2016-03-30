package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskAttack
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.stats
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 30-Mar-16.
 */

class ActionAttack(): AbstractAction()
{
	lateinit var key: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val target = getData( key, null ) as? Point;
		val posData = Mappers.position.get(entity)
		val taskData = Mappers.task.get(entity)
		val tile = posData.position as? Tile
		val stats = entity.stats()

		state = ExecutionState.FAILED;
		// doesnt have all the needed data, fail
		if ( target == null || posData == null || tile == null || taskData == null || stats == null )
		{
			return state;
		}

		val targetTile = tile.level?.getTile(target) ?: return state

		var canAttack = false
		for (e in targetTile.contents)
		{
			val estats = e.stats() ?: continue

			if (estats.factions.intersect(stats.factions).size == 0)
			{
				canAttack = true
				break
			}
		}

		if (!canAttack) return state

		val dir = Enums.Direction.getDirection(tile, target)
		val attack = TaskAttack(dir)

		taskData.tasks.add(attack)

		state = ExecutionState.COMPLETED;
		return state;
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.getAttribute("Key")
	}

	override fun cancel()
	{

	}

}