package com.lyeeedar.AI.BehaviourTree.Conditionals

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.stats
import com.lyeeedar.Components.tile

/**
 * Created by Philip on 21-Mar-16.
 */

class ConditionalCheckValue(): AbstractConditional()
{
	//----------------------------------------------------------------------
	lateinit var condition: String
	var succeed: ExecutionState = ExecutionState.COMPLETED
	var fail: ExecutionState = ExecutionState.FAILED

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		if (condition.startsWith("dist "))
		{
			val split = condition.split(' ')
			val key1 = split[1]
			val key2 = split[2]

			val p1 = getData(key1, entity.tile()!!)!!
			val p2 = getData(key2, entity.tile()!!)!!

			val dist = p1.dist(p2)

			val conditionEqn = dist.toString() + split[3]

			val variableMap = getVariableMap()

			entity.stats()?.write(variableMap)

			val conditionVal = conditionEqn.evaluate(variableMap)

			state = if (conditionVal != 0f) succeed else fail
			return state
		}
		else
		{
			val variableMap = getVariableMap()

			entity.stats()?.write(variableMap)

			val conditionVal = condition.evaluate(variableMap)

			state = if (conditionVal != 0f) succeed else fail
			return state
		}
	}

	//----------------------------------------------------------------------
	override fun cancel(entity: Entity)
	{

	}

	//----------------------------------------------------------------------
	override fun parse(xml: XmlReader.Element)
	{
		this.succeed = ExecutionState.valueOf(xml.getAttribute("Success", "COMPLETED").toUpperCase())
		this.fail = ExecutionState.valueOf(xml.getAttribute("Failure", "FAILED").toUpperCase())

		this.condition = xml.getAttribute("Condition").toLowerCase().unescapeCharacters()
	}
}