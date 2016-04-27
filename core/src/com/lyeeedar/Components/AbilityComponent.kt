package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Ability.Ability
import com.lyeeedar.Ability.AbilityChain
import com.lyeeedar.Ability.Targetting.AbilityWrapper

/**
 * Created by Philip on 23-Apr-16.
 */

class AbilityComponent: Component
{
	constructor()

	val abilities: com.badlogic.gdx.utils.Array<AbilityWrapper> = com.badlogic.gdx.utils.Array()
	var current: AbilityWrapper? = null
}