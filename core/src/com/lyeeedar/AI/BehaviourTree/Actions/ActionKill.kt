package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.MarkedForDeletionComponent

class ActionKill : AbstractAction()
{
	override fun evaluate(entity: Entity): ExecutionState
	{
		if (entity.getComponent(MarkedForDeletionComponent::class.java) == null) entity.add(MarkedForDeletionComponent())
		return ExecutionState.COMPLETED
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun cancel(entity: Entity)
	{
	}
}