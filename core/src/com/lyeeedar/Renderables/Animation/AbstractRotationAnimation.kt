package com.lyeeedar.Renderables.Animation

import com.lyeeedar.Util.Colour

abstract class AbstractRotationAnimation : AbstractAnimation()
{
	override fun renderScale(): FloatArray? = null
	override fun renderOffset(): FloatArray? = null
	override fun renderColour(): Colour? = null
}