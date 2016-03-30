package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class SelectorUntil(): AbstractSelector()
{
	lateinit var until: ExecutionState

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED;

		var i = 0;
		while (i < nodes.size)
		{
			val temp = nodes.get(i).evaluate(entity);
			if (temp == until)
			{
				state = ExecutionState.COMPLETED;
				break;
			}
			i++
		}
		i++;
		while (i < nodes.size)
		{
			nodes.get(i).cancel();
			i++
		}

		return state;
	}

	//----------------------------------------------------------------------
	override fun cancel()
	{
		for (i in 0..nodes.size-1)
		{
			nodes.get(i).cancel();
		}
	}

	//----------------------------------------------------------------------
	override fun parse(xml: XmlReader.Element)
	{
		super.parse(xml);

		until = ExecutionState.valueOf(xml.getAttribute("State").toUpperCase());
	}
}