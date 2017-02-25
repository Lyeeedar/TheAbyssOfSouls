package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.BehaviourTree.BehaviourTree
import com.lyeeedar.AI.IAI
import com.lyeeedar.AI.Tasks.AbstractTask

class TaskComponent: Component
{
	constructor(ai: IAI)
	{
		this.ai = ai
	}

	constructor(path: String)
	{
		ai = BehaviourTree.load(path)
	}

	val ai: IAI
	val tasks: com.badlogic.gdx.utils.Array<AbstractTask> = com.badlogic.gdx.utils.Array()
}