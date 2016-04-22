package com.lyeeedar.Ability.Cost

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 22-Apr-16.
 */

class CostCharge(): AbstractCost()
{
	var charges: Int = 1

	override fun parse(xml: XmlReader.Element)
	{
		charges = xml.text.toInt()
	}

	override fun isCostAvailable(entity: Entity): Boolean
	{
		return charges > 0
	}

	override fun spendCost(entity: Entity)
	{
		charges--
	}
}