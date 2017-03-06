package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.tile
import com.lyeeedar.Components.trailing
import com.lyeeedar.Direction
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionSetValue(): AbstractAction()
{
	lateinit var key: String
	lateinit var value: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		if (value.startsWith("move "))
		{
			val split = value.split(' ')
			val pointKey = split[1]
			val direction = Direction.valueOf(split[2].toUpperCase())
			val dist = split[3].toInt()

			val point = getData(pointKey, entity.tile())!!

			val newPoint = Point.obtain().set(point.x + direction.x * dist, point.y + direction.y * dist)

			setData(key, newPoint)
		}
		else if (value.startsWith("trail "))
		{
			val action = value.removePrefix("trail ")

			if (action == "length")
			{
				val trail = entity.trailing() ?: return ExecutionState.FAILED
				val length = trail.entities.size-1
				setData(key, length)
			}
			else throw Exception("Unknown trail set value action '$action'!")
		}
		else if (value == "entitypos")
		{
			setData(key, entity.tile()!!)
		}
		else
		{
			try
			{
				val value = value.evaluate(getVariableMap())
				setData(key, value)
			}
			catch (ex: Exception)
			{
				setData(key, value)
			}
		}

		state = ExecutionState.COMPLETED
		return state;
	}

	override fun cancel(entity: Entity)
	{

	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.getAttribute("Key").toLowerCase()
		value = xml.getAttribute("Value").toLowerCase().unescapeCharacters()
	}
}