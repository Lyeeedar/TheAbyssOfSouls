package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class DecoratorInvert(): AbstractDecorator()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		state = node?.evaluate(entity) ?: ExecutionState.FAILED;

		if (state == ExecutionState.COMPLETED) { return ExecutionState.FAILED; }
		else if (state == ExecutionState.FAILED) { return ExecutionState.COMPLETED; }
		else { return ExecutionState.RUNNING; }
	}
}