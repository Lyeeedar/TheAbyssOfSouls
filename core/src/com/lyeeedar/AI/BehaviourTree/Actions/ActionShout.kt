package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 20-Apr-16.
 */

class ActionShout(): AbstractAction()
{
	lateinit var key: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		val keyVal = getData(key, null) ?: return ExecutionState.FAILED



		return ExecutionState.COMPLETED
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.getAttribute("Key").toLowerCase()
	}

	override fun cancel()
	{

	}
}