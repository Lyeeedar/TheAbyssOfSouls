package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Ability.Ability

/**
 * Created by Philip on 23-Apr-16.
 */

class AbilityComponent: Component
{
	constructor()

	val abilities: com.badlogic.gdx.utils.Array<AbilityChain> = com.badlogic.gdx.utils.Array()
	var current: AbilityChain? = null
}

class AbilityChain()
{
	lateinit var ability: Ability
	val next: com.badlogic.gdx.utils.Array<AbilityChain> = com.badlogic.gdx.utils.Array()
}