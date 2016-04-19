package com.lyeeedar.AI.BehaviourTree.Decorators

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class DecoratorSetState(): AbstractDecorator()
{
	var completed: ExecutionState = ExecutionState.COMPLETED
	var failed: ExecutionState = ExecutionState.FAILED
	var running: ExecutionState = ExecutionState.RUNNING

	override fun evaluate(entity: Entity): ExecutionState
	{
		val retState = node?.evaluate(entity);

		if (retState == ExecutionState.COMPLETED)
		{
			state = completed;
		}
		else if (retState == ExecutionState.RUNNING)
		{
			state = running;
		}
		else if (retState == ExecutionState.FAILED)
		{
			state = failed;
		}

		return state;
	}

	override fun parse(xml: XmlReader.Element)
	{
		super.parse(xml);

		if (xml.getAttribute("State", null) != null)
		{
			val state = ExecutionState.valueOf(xml.getAttribute("State").toUpperCase());
			completed = state
			running = state
			failed = state
		}

		if (xml.get("Completed", null) != null) { completed = ExecutionState.valueOf(xml.getAttribute("Completed").toUpperCase()); }
		if (xml.get("Running", null) != null) { running = ExecutionState.valueOf(xml.getAttribute("Running").toUpperCase()); }
		if (xml.get("Failed", null) != null) { failed = ExecutionState.valueOf(xml.getAttribute("Failed").toUpperCase()); }
	}
}