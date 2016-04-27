package com.lyeeedar.Ability

/**
 * Created by Philip on 27-Apr-16.
 */

class AbilityChain()
{
	lateinit var ability: Ability
	val next: com.badlogic.gdx.utils.Array<AbilityChain> = com.badlogic.gdx.utils.Array()

	var used: Boolean = false
}