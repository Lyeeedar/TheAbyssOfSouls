package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState

/**
 * Created by Philip on 19-Apr-16.
 */

class ActionIncrement(): AbstractAction()
{
	lateinit var key: String
	var change: Int = 1

	override fun evaluate(entity: Entity): ExecutionState
	{
		val rawVal = getData(key, 0)
		val keyVal = if (rawVal is String) rawVal.toInt() else rawVal as Int
		setData(key, keyVal + change)

		state = ExecutionState.COMPLETED
		return state
	}

	override fun parse(xml: XmlReader.Element)
	{
		if (xml.name == "Decrement") change = -1
		change = xml.getInt("Change", change)

		key = xml.get("Key").toLowerCase()
	}

	override fun cancel()
	{
	}
}