package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.tile
import com.lyeeedar.Level.Room
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 29-Mar-16.
 */

class ActionConvertTo(): AbstractAction()
{
	lateinit var input: String
	lateinit var output: String
	lateinit var type: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val obj: Any? = getData(input, null)
		var out: Any? = null

		if (obj is Entity)
		{
			out = obj.tile() ?: Mappers.position.get(obj).position
		}
		else if (obj is Room)
		{
			out = Point.obtain().set(obj.x + obj.width/2, obj.y + obj.height/2)
		}

		if (out != null)
		{
			parent.setData(output, out)
			state = ExecutionState.COMPLETED
		}
		else
		{
			state = ExecutionState.FAILED
		}

		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		input = xml.get("Input").toLowerCase()
		output = xml.get("Output").toLowerCase()
		type = xml.get("Type", "Position").toLowerCase()
	}

	override fun cancel()
	{

	}
}