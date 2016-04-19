package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.tile
import com.lyeeedar.Level.Room

/**
 * Created by Philip on 19-Apr-16.
 */

class ActionGetRoom(): AbstractAction()
{
	var metaVal: String? = null
	lateinit var key: String
	var neighbour: Boolean = false

	override fun evaluate(entity: Entity): ExecutionState
	{
		val tile = entity.tile() ?: return ExecutionState.FAILED

		val currentRoom = tile.level.getRoom(tile)
		var targetRoom: Room? = null

		if (neighbour)
		{
			if (currentRoom != null)
			{
				targetRoom = currentRoom.neighbours.random()
			}
			else
			{
				var minDist = Int.MAX_VALUE
				for (room in tile.level.rooms)
				{
					val dst = tile.dist(room.x+room.width/2, room.y+room.height/2)
					if (dst < minDist)
					{
						minDist = dst
						targetRoom = room
					}
				}
			}
		}
		else
		{
			if (metaVal != null)
			{
				val valid = com.badlogic.gdx.utils.Array<Room>()
				for (room in tile.level.rooms)
				{
					if (room != currentRoom && room.metavalues.contains(metaVal))
					{
						valid.add(room)
					}
				}

				if (valid.size > 0)
				{
					targetRoom = valid.random()
				}
			}
			else
			{
				val valid = com.badlogic.gdx.utils.Array<Room>()
				for (room in tile.level.rooms)
				{
					if (room != currentRoom)
					{
						valid.add(room)
					}
				}

				if (valid.size > 0)
				{
					targetRoom = valid.random()
				}
			}
		}

		if (targetRoom == null)
		{
			state = ExecutionState.FAILED
			return state
		}

		setData(key, targetRoom)
		state = ExecutionState.COMPLETED
		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		neighbour = xml.name == "GetNeighbourRoom"
		key = xml.getAttribute("Key")
		metaVal = xml.getAttribute("MetaVal", null)
	}

	override fun cancel()
	{

	}
}