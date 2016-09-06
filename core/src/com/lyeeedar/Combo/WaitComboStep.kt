package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction

class WaitComboStep: ComboStep()
{
	override fun isValid(entity: Entity, direction: Direction): Boolean
	{
		for (step in nextSteps)
		{
			if (step.isValid(entity, direction)) return true
		}

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun doActivate(entity: Entity, direction: Direction)
	{
		println("doing wait")
	}
}