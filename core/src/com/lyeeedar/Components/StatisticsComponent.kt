package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectIntMap
import com.badlogic.gdx.utils.OrderedSet
import com.lyeeedar.ElementType
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.ciel

class StatisticsComponent: Component
{
	val factions: OrderedSet<String> = OrderedSet()

	var hp: Float = 0f
		get() = field
		set(value)
		{
			var v = Math.min(value, maxHP)

			var diff = v - hp
			if (diff < 0)
			{
				if (invulnerable)
				{
					blockedDamage = true
					return
				}
				else if (blocking)
				{
					stamina += diff
					if (stamina < 0)
					{
						blocking = false
						v = hp + stamina
						diff = stamina

						blockedDamage = false
						blockBroken = true
					}
					else
					{
						blockedDamage = true
					}
				}
			}

			field = v
			if (godMode && field < 1) field = 1f

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
			field = v
		}

	var maxStamina: Float = 0f
		get() = field
		set(value)
		{
			field = value

			if (stamina < value) stamina = value
		}

	val resistances = FastEnumMap<ElementType, Int>(ElementType::class.java)

	var sight: Float = 0f

	var blocking = false
	var invulnerable = false
	var godMode = false

	var blockedDamage = false
	var blockBroken = false
	var insufficientStamina = 0f

	fun dealDamage(amount: Int, element: ElementType, elementalConversion: Float)
	{
		var elementalDam = (amount * elementalConversion).ciel()
		val baseDam = amount - elementalDam

		hp -= baseDam

		val resistance = resistances[element] ?: 0
		elementalDam += (elementalDam.toFloat() * 0.25f * resistance.toFloat()).ciel()

		hp -= elementalDam
	}

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