package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class DecoratorDataScope(): AbstractDecorator()
{
	init
	{
		data = ObjectMap<String, Any>()
	}

	override fun evaluate(entity: Entity): ExecutionState
	{
		return node?.evaluate(entity) ?: ExecutionState.FAILED;
	}
}