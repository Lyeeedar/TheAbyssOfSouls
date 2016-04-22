package com.lyeeedar.Ability.Cost

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Ability.Ability
import java.util.*

/**
 * Created by Philip on 22-Apr-16.
 */

abstract class AbstractCost()
{
	abstract fun parse(xml: XmlReader.Element)
	abstract fun isCostAvailable(entity: Entity): Boolean
	abstract fun spendCost(entity: Entity)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractCost
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractCost

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractCost>
		{
			val type = when(name) {
				"STAMINA" -> CostStamina::class.java
				"HP", "HEALTH" -> CostHealth::class.java
				"CHARGE", "CHARGES" -> CostCharge::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid ability cost type: $name")
			}

			return type
		}
	}
}