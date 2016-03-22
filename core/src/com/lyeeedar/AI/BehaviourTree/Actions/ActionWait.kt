package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Components.Mappers

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionWait(): AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		val task = Mappers.task.get(entity)
		task.tasks.add(TaskWait())

		state = ExecutionState.COMPLETED
		return state
	}

	override fun parse(xml: XmlReader.Element) {

	}

	override fun cancel() {

	}

}