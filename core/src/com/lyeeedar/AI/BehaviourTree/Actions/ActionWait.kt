package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.AI.Tasks.TaskWait
import com.lyeeedar.Components.Mappers

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionWait(): AbstractAction()
{
	lateinit var count: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val num = Math.round(EquationHelper.evaluate(count)).toInt()

		val task = Mappers.task.get(entity)

		for (i in 0..num-1)
		{
			task.tasks.add(TaskWait())
		}

		state = ExecutionState.COMPLETED
		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		count = xml.getAttribute("Count", "1").toLowerCase()
	}

	override fun cancel() {

	}

}