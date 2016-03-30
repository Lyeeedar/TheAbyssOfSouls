package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.StatisticsComponent
import com.lyeeedar.Components.postEvent
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 29-Mar-16.
 */

class EventActionDamage(group: EventActionGroup): IteratingEventAction(group, Family.all(StatisticsComponent::class.java).get())
{
	constructor(group: EventActionGroup, stats: FastEnumMap<Enums.Statistic, Float>) : this(group)
	{
		for (stat in Enums.Statistic.values())
		{
			damMap.put(stat, stats.get(stat))
		}
	}

	val damMap: FastEnumMap<Enums.Statistic, Float> = Enums.Statistic.getStatisticsBlock()

	override fun handle(args: EventArgs, entity: Entity)
	{
		val stats = Mappers.stats.get(entity)

		var totalDam = 0f
		for (i in 0..Enums.Statistic.ATTACK_STATS.size-1)
		{
			val atkStat = Enums.Statistic.ATTACK_STATS[i]
			val defStat = Enums.Statistic.DEFENSE_STATS[i]

			val atk = damMap.get(atkStat) ?: 0f
			if (atk == 0f) continue

			val def = stats.stats.get(defStat) ?: 0f

			val dam = Math.max(0f, atk - def)

			totalDam += dam
		}

		stats.hp -= totalDam
		entity.postEvent(EventArgs(EventComponent.EventType.DAMAGED, null, entity, totalDam))
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)

			damMap.put(Enums.Statistic.valueOf(el.name), el.text.toFloat())
		}
	}

}