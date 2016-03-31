package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.position
import com.lyeeedar.Components.tile

/**
 * Created by Philip on 31-Mar-16.
 */

class ActionTelegraphedAttack(): AbstractAction()
{

	override fun evaluate(entity: Entity): ExecutionState
	{
		val atk = Mappers.telegraphed.get(entity)
		val pos = entity.position()
		val tile = entity.tile()

		if (atk.currentCombo != null)
		{
			if (atk.readyEntity == null)
			{
				// advance combo
			}
			else
			{
				// do actual attack
			}
		}
		else
		{
			// find all valid combos

			// weight and pick random

			// prepare first attack in combo
		}

		return state
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun cancel()
	{

	}
}
