package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Ability.Ability
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 23-Apr-16.
 */

class TaskUseAbility(): AbstractTask(EventComponent.EventType.NONE)
{
	lateinit var ability: Ability

	constructor(ability: Ability) : this()
	{
		this.ability = ability
	}

	override fun execute(e: Entity)
	{
		for (cost in ability.costs)
		{
			cost.spendCost(e)
		}
		EffectApplier.apply(e, Point.ZERO, ability.dir, ability.targetTile ?: Point.ZERO, ability.hitPoints, ability.hitType, ability.hitSprite, ability.effectData)
	}
}