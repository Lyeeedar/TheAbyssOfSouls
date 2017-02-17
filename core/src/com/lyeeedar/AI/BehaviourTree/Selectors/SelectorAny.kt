package com.lyeeedar.AI.BehaviourTree.Selectors

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class SelectorAny(): AbstractSelector()
{
	val runningList: com.badlogic.gdx.utils.Array<Int> = com.badlogic.gdx.utils.Array<Int>()
	var reset: Boolean = false

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = ExecutionState.FAILED;

		if (runningList.size > 0)
		{
			val itr = runningList.iterator();
			while (itr.hasNext())
			{
				val temp = nodes.get(itr.next()).evaluate(entity);

				if (state != ExecutionState.RUNNING && temp == ExecutionState.COMPLETED)
				{
					state = temp;
					itr.remove();
				}
				else if (temp == ExecutionState.RUNNING)
				{
					state = temp;
				}
				else
				{
					itr.remove();
				}
			}
		}
		else
		{
			for (i in 0..nodes.size-1)
			{
				val temp = nodes.get(i).evaluate(entity);
				if (state != ExecutionState.RUNNING && temp == ExecutionState.COMPLETED)
				{
					state = temp;
				}
				else if (temp == ExecutionState.RUNNING)
				{
					state = temp;
					runningList.add(i);
				}
			}
		}

		if (reset)
		{
			runningList.clear();
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

		runningList.clear();
	}

	//----------------------------------------------------------------------
	override fun parse(xml: XmlReader.Element)
	{
		super.parse(xml);
		reset = xml.getBooleanAttribute("Reset", false);
	}
}