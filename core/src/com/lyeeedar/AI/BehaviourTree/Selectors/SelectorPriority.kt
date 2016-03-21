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
		while (i++ < nodes.size)
		{
			val temp = nodes.get(i).evaluate(entity);
			if (temp != ExecutionState.FAILED)
			{
				state = temp;
				break;
			}
		}

		i++;
		while (i++ < nodes.size)
		{
			nodes.get(i).cancel();
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
}