package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class WaitComboStep: ComboStep()
{
	override fun isValid(entity: Entity): Boolean = true

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun doActivate(entity: Entity)
	{

	}
}