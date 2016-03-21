package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class DecoratorRepeat(): AbstractDecorator()
{
	var until: ExecutionState? = null
	var repeats: Int = 1
	var i: Int = 0

	override fun evaluate(entity: Entity): ExecutionState
	{
		val retState = node?.evaluate(entity);

		if (until != null)
		{
			if (retState == until)
			{
				state = ExecutionState.COMPLETED
				return state
			}
		}

		i++;

		if (i == repeats)
		{
			state = ExecutionState.COMPLETED
			return state
		}

		state = ExecutionState.RUNNING
		return state
	}

	override fun cancel()
	{
		super.cancel()
		i = 0
	}

	override fun parse(xml: XmlReader.Element)
	{
		super.parse(xml);

		if (xml.getAttribute("Until", null) != null)
		{
			until = ExecutionState.valueOf(xml.getAttribute("State").toUpperCase());
		}

		repeats = xml.getInt("Repeats", -1);
	}
}