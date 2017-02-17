package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Combo.ComboTree

class ComboComponent: Component
{
	val combos = Array<ComboTree>()

	var currentCombo: ComboTree? = null
}