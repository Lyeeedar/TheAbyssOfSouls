package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedSet
import com.lyeeedar.Enums
import com.lyeeedar.Util.FastEnumMap
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class StatisticsComponent: Component
{
	constructor()

	val stats: FastEnumMap<Enums.Statistic, Float> = Enums.Statistic.getStatisticsBlock(0f)
	val variableMap: ObjectFloatMap<String> = ObjectFloatMap()
		get()
		{
			field.put("morale", morale)
			return field
		}
	val factions: OrderedSet<String> = OrderedSet()
	var hp: Float
		get() = stats.get(Enums.Statistic.HEALTH)
		set(value)
		{
			val diff = value - hp
			stats[Enums.Statistic.HEALTH] = value
			morale += (diff / hp) * moraleChange

			damTally += diff
		}
	var damTally: Float = 0f

	var stamina: Float
		get() = stats[Enums.Statistic.STAMINA]
		set(value) { stats[Enums.Statistic.STAMINA] = value }
	var morale: Float
		get() = stats[Enums.Statistic.MORALE]
		set(value) { stats[Enums.Statistic.MORALE] = value }
	val moraleChange: Float
		get() = stats[Enums.Statistic.MORALE_CHANGE]
}