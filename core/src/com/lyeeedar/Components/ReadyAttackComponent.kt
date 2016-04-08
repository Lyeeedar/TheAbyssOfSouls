package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.Actions.ActionTelegraphedAttack

/**
 * Created by Philip on 08-Apr-16.
 */

class ReadyAttackComponent: Component
{
	constructor(parent: Entity, action: ActionTelegraphedAttack)
	{
		this.parent = parent
		this.action = action
	}

	val action: ActionTelegraphedAttack
	val parent: Entity
}