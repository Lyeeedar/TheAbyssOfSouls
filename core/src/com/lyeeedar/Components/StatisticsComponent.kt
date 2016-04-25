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

	val attack: FastEnumMap<Enums.ElementType, Float> = Enums.ElementType.getElementMap(0f)
	val defense: FastEnumMap<Enums.ElementType, Float> = Enums.ElementType.getElementMap(0f)
	val power: FastEnumMap<Enums.ElementType, Float> = Enums.ElementType.getElementMap(1f)

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
			val v = Math.min(value, maxHP)

			val diff = v - hp
			stats[Enums.Statistic.HEALTH] = v
			morale += (diff / hp) * moraleChange

			if (diff < 0)
			{
				bonusHP = diff / 2f
			}
			else
			{
				bonusHP = Math.max(0f, bonusHP - diff)
			}
		}
	var bonusHP: Float = 0f

	var maxHP: Float
		get() = stats[Enums.Statistic.MAX_HEALTH]
		set(value) { stats[Enums.Statistic.MAX_HEALTH] = value }

	var stamina: Float
		get() = stats[Enums.Statistic.STAMINA]
		set(value)
		{
			val v = Math.min(maxStamina, value)

			val diff = v - stamina
			stats[Enums.Statistic.STAMINA] = v
			if (diff < 0)
			{
				staminaReduced = true
			}
		}
	var staminaReduced: Boolean = false

	var maxStamina: Float
		get() = stats[Enums.Statistic.MAX_STAMINA]
		set(value) { stats[Enums.Statistic.MAX_STAMINA] = value }

	var morale: Float
		get() = stats[Enums.Statistic.MORALE]
		set(value) { stats[Enums.Statistic.MORALE] = value }
	val moraleChange: Float
		get() = stats[Enums.Statistic.MORALE_CHANGE]
}