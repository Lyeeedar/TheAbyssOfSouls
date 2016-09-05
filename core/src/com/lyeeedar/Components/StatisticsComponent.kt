package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.OrderedSet
import com.lyeeedar.ElementType
import com.lyeeedar.Statistic
import com.lyeeedar.Util.FastEnumMap
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class StatisticsComponent: Component
{
	constructor()

	val attack: FastEnumMap<ElementType, Float> = ElementType.getElementMap(0f)
	val defense: FastEnumMap<ElementType, Float> = ElementType.getElementMap(0f)
	val power: FastEnumMap<ElementType, Float> = ElementType.getElementMap(1f)

	val stats: FastEnumMap<Statistic, Float> = Statistic.getStatisticsBlock(1f)
	val variableMap: ObjectFloatMap<String> = ObjectFloatMap()
		get()
		{
			field.put("morale", morale)
			return field
		}
	val factions: OrderedSet<String> = OrderedSet()
	var hp: Float
		get() = stats.get(Statistic.HEALTH)
		set(value)
		{
			val v = Math.min(value, maxHP)

			val diff = v - hp
			stats[Statistic.HEALTH] = v
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
		get() = stats[Statistic.MAX_HEALTH]
		set(value)
		{
			stats[Statistic.MAX_HEALTH] = value

			if (hp < value) hp = value
		}

	var stamina: Float
		get() = stats[Statistic.STAMINA]
		set(value)
		{
			val v = Math.min(maxStamina, value)

			val diff = v - stamina
			stats[Statistic.STAMINA] = v
			if (diff < 0)
			{
				staminaReduced = true
			}
		}
	var staminaReduced: Boolean = false

	var maxStamina: Float
		get() = stats[Statistic.MAX_STAMINA]
		set(value)
		{
			stats[Statistic.MAX_STAMINA] = value

			if (stamina < value) stamina = value
		}

	var morale: Float
		get() = stats[Statistic.MORALE]
		set(value) { stats[Statistic.MORALE] = value }
	val moraleChange: Float
		get() = stats[Statistic.MORALE_CHANGE]
}

fun OrderedSet<String>.isAllies(other: OrderedSet<String>): Boolean
{
	for (faction in this)
	{
		if (other.contains(faction)) return true
	}

	return false
}