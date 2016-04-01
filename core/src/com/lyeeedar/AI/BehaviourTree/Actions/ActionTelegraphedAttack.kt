package com.lyeeedar.AI.BehaviourTree.Actions

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AI.BehaviourTree.ExecutionState
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.position
import com.lyeeedar.Components.tile
import com.lyeeedar.GlobalData

/**
 * Created by Philip on 31-Mar-16.
 */

class ActionTelegraphedAttack(): AbstractAction()
{

	override fun evaluate(entity: Entity): ExecutionState
	{
		val atkData = Mappers.telegraphed.get(entity)
		val pos = entity.position()

		if (atkData.currentCombo != null)
		{
			if (atkData.readyEntity == null)
			{
				// advance combo
			}
			else
			{
				// do actual attack
				GlobalData.Global.engine?.removeEntity(atkData.readyEntity)
				val combo = atkData.currentComboStep
				val atk = atkData.currentAttack

				// move forward if able
				if (combo.canMove)
				{
					// try to move forward
				}

				// actually do the attack
				if (atk.hitType.equals("all"))
				{

				}
			}
		}
		else
		{
			// find all valid combos + starttile + direction

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
