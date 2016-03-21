package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 21-Mar-16.
 */

class ActionClearValue(): AbstractAction()
{
	lateinit var key: String

	override fun evaluate(entity: Entity): ExecutionState
	{
		parent.setData(key, null);

		state = ExecutionState.COMPLETED
		return state;
	}

	override fun cancel()
	{

	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.getAttribute("Key");
	}
}