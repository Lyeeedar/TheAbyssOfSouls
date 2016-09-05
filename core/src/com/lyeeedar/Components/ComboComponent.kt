package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Combo.ComboStep

class ComboComponent: Component
{
	val combos = Array<ComboStep>()

	var currentCombo: ComboStep? = null
}
