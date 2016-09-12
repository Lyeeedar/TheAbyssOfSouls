package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction

class WaitComboStep: ComboStep()
{
	override fun isValid(entity: Entity, direction: Direction, comboTree: ComboTree): Boolean
	{
		for (step in comboTree.next)
		{
			if (step.current.isValid(entity, direction, step)) return true
		}

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun doActivate(entity: Entity, direction: Direction)
	{
	}
}