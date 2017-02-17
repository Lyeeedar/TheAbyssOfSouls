package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class SelectorSequence(): AbstractSelector()
{
	//----------------------------------------------------------------------
	var reset: Boolean = false
	var i: Int = 0

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.COMPLETED;

		while (i < nodes.size)
		{
			val temp = nodes.get(i).evaluate(entity);
			if (temp != ExecutionState.COMPLETED)
			{
				state = temp;
				break;
			}
			i++
		}

		if (state != ExecutionState.RUNNING)
		{
			i = 0;
			while (i < nodes.size)
			{
				nodes.get(i).cancel(entity);
				i++
			}
			i = 0;
		}

		if (reset)
		{
			i = 0;
		}

		return state;
	}

	//----------------------------------------------------------------------
	override fun cancel(entity: Entity)
	{
		for (i in 0..nodes.size-1)
		{
			nodes.get(i).cancel(entity);
		}
		i = 0;
	}

	//----------------------------------------------------------------------
	override fun parse(xml: XmlReader.Element)
	{
		super.parse(xml);

		reset = xml.getBooleanAttribute("Reset", false);
	}
}