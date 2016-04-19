package com.lyeeedar.AI.BehaviourTree.Conditionals

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.stats
import com.lyeeedar.Enums
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class ConditionalCheckValue(): AbstractConditional()
{
	//----------------------------------------------------------------------
	var key: String? = null
	var condition: String? = null
	lateinit var reliesOn: Array<String>
	var succeed: ExecutionState = ExecutionState.COMPLETED
	var fail: ExecutionState = ExecutionState.FAILED

	//----------------------------------------------------------------------
	override fun evaluate(entity: Entity): ExecutionState
	{
		var keyVal = 0f;

		val k = key
		if (k != null)
		{
			val storedValue = getData(k, null);

			if (condition == null)
			{
				state = if (storedValue != null) succeed else fail;
				return state;
			}
			else
			{
				if (storedValue is Boolean)
				{
					keyVal = if (storedValue) 1f else 0f;
				}
				else if (storedValue is Int)
				{
					keyVal = storedValue.toFloat();
				}
				else if (storedValue is Float)
				{
					keyVal = storedValue;
				}
				else
				{
					keyVal = if (storedValue != null) 1f else 0f;
				}
			}
		}

		val stats = entity.stats()
		val variableMap = stats.variableMap;
		if (k != null)
		{
			variableMap.put( k.toLowerCase(), keyVal );
		}

		for (s in reliesOn)
		{
			if (!variableMap.containsKey( s ))
			{
				variableMap.put( s, 0f );
			}
		}

		val conditionVal = EquationHelper.evaluate( condition, variableMap );

		state = if (conditionVal != 0f) succeed else fail;
		return state;
	}

	//----------------------------------------------------------------------
	override fun cancel()
	{

	}

	//----------------------------------------------------------------------
	override fun parse(xml: XmlReader.Element)
	{
		this.key = xml.getAttribute("Key", null)?.toLowerCase()
		this.succeed = ExecutionState.valueOf(xml.getAttribute("Success", "COMPLETED").toUpperCase());
		this.fail = ExecutionState.valueOf(xml.getAttribute("Failure", "FAILED").toUpperCase());

		this.condition = xml.getAttribute("Condition", null)?.toLowerCase();

		reliesOn = xml.getAttribute( "ReliesOn", "" ).toLowerCase().split( "," ).toTypedArray();
	}
}