package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Ability.Ability
import com.lyeeedar.Ability.Targetting.AbilityWrapper
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 23-Apr-16.
 */

class TaskUseAbility(): AbstractTask(EventComponent.EventType.NONE)
{
	lateinit var ability: AbilityWrapper

	constructor(ability: AbilityWrapper) : this()
	{
		this.ability = ability
	}

	override fun execute(e: Entity)
	{
		if (ability.current.used) throw RuntimeException("Ability already used!")

		val ab = ability.current.ability

		for (cost in ab.costs)
		{
			cost.spendCost(e)
		}
		EffectApplier.apply(e, Point.ZERO, ab.dir, ab.targetTile ?: Point.ZERO, ab.hitPoints, ab.hitType, ab.hitSprite, ab.effectData)

		if (ability.current.next.size > 0)
		{
			ability.current.used = true
		}
		else
		{
			ability.current = ability.root

			val abilityData = Mappers.ability.get(e)
			abilityData.current = null
		}
	}
}