package com.lyeeedar.Ability.Cost

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.stats

/**
 * Created by Philip on 22-Apr-16.
 */

class CostHealth(): AbstractCost()
{
	var cost: Float = 0f

	override fun parse(xml: XmlReader.Element)
	{
		cost = xml.text.toFloat()
	}

	override fun isCostAvailable(entity: Entity): Boolean
	{
		val stats = entity.stats() ?: return false
		return stats.hp > cost
	}

	override fun spendCost(entity: Entity)
	{
		val stats = entity.stats() ?: return
		stats.hp -= cost
	}
}