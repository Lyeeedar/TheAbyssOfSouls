package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 31-Jul-16.
 */

abstract class AbstractScaleAnimation() : AbstractAnimation()
{
	override fun renderOffset(): FloatArray? = null
	override fun renderColour(): Colour? = null
}