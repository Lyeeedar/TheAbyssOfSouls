package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.OrderedSet
import com.lyeeedar.ElementType
import com.lyeeedar.Statistic
import com.lyeeedar.Util.FastEnumMap

class StatisticsComponent: Component
{
	constructor()

	val stats: FastEnumMap<Statistic, Float> = Statistic.getStatisticsBlock(10f)

	val variableMap: ObjectFloatMap<String> = ObjectFloatMap()
		get()
		{
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
}

fun OrderedSet<String>.isAllies(other: OrderedSet<String>): Boolean
{
	for (faction in this)
	{
		if (other.contains(faction)) return true
	}

	return false
}