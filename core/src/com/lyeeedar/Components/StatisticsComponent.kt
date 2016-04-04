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

	var name: String = ""
	val stats: FastEnumMap<Enums.Statistic, Float> = Enums.Statistic.getStatisticsBlock(0f)
	val variableMap: ObjectFloatMap<String> = ObjectFloatMap()
		get()
		{
			field.put("morale", morale)
			return field
		}
	val factions: OrderedSet<String> = OrderedSet()
	var hp: Float = 1f
		set(value)
		{
			val diff = value - field
			field = value
			morale += (diff / stats.get(Enums.Statistic.MAX_HEALTH)) * 100f

			damTally += diff
		}
	var damTally: Float = 0f

	var stamina: Float = 1f
	var morale: Float = 100f
}