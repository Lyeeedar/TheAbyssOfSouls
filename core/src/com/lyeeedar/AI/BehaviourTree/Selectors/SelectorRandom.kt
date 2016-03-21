package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class SelectorRandom(): AbstractSelector()
{
	//----------------------------------------------------------------------
	val ran: java.util.Random = java.util.Random();
	val numList: com.badlogic.gdx.utils.Array<Int> = com.badlogic.gdx.utils.Array<Int>(false, 16);
	var i: Int = -1

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED;

		if (i == -1)
		{
			numList.clear();
			for (i in 0..nodes.size-1) { numList.add(i); }

			while (state == ExecutionState.FAILED && numList.size > 0)
			{
				var ti = ran.nextInt(numList.size);
				i = numList.get(ti);
				numList.removeIndex(ti);

				state = nodes.get(i).evaluate(entity);
			}
		}

		if (state != ExecutionState.RUNNING)
		{
			i = -1;
		}

		return state;
	}

	//----------------------------------------------------------------------
	override fun cancel()
	{
		i = -1;

		for (i in 0..nodes.size-1)
		{
			nodes.get(i).cancel();
		}
	}
}