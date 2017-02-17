package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class SelectorPriority(): AbstractSelector()
{
	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED;

		var i = 0;
		while (i < nodes.size)
		{
			val temp = nodes.get(i).evaluate(entity);
			if (temp != ExecutionState.FAILED)
			{
				state = temp;
				break;
			}

			i++
		}

		i++;
		while (i < nodes.size)
		{
			nodes.get(i).cancel(entity);
			i++
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
	}
}