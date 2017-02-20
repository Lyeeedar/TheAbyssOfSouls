package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.OrderedSet

class StatisticsComponent: Component
{
	val factions: OrderedSet<String> = OrderedSet()

	var hp: Float = 0f
		get() = field
		set(value)
		{
			val v = Math.min(value, maxHP)

			val diff = v - hp
			field = v

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

	var maxHP: Float = 0f
		get() = field
		set(value)
		{
			field = value

			if (hp < value) hp = value
		}

	var stamina: Float = 0f
		get() = field
		set(value)
		{
			val v = Math.min(maxStamina, value)

			val diff = v - stamina
			field = v
			if (diff < 0)
			{
				staminaReduced = true
			}
		}
	var staminaReduced: Boolean = false

	var maxStamina: Float = 0f
		get() = field
		set(value)
		{
			field = value

			if (stamina < value) stamina = value
		}

	var sight: Float = 0f

	operator fun get(key: String, fallback: Float? = null): Float
	{
		return when (key.toLowerCase())
		{
			"hp" -> hp
			"maxhp" -> maxHP
			"stamina" -> stamina
			"maxstamina" -> maxStamina
			else -> fallback ?: throw Exception("Unknown statistic '$key'!")
		}
	}

	fun write(variableMap: ObjectFloatMap<String>): ObjectFloatMap<String>
	{
		variableMap.put("hp", hp)
		variableMap.put("maxhp", maxHP)
		variableMap.put("stamina", stamina)
		variableMap.put("maxstamina", maxStamina)

		return variableMap
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