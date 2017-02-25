package com.lyeeedar.Renderables.Animation

/**
 * Created by Philip on 31-Jul-16.
 */

abstract class AbstractColourAnimation() : AbstractAnimation()
{
	var oneTime = true

	override fun renderScale(): FloatArray? = null
	override fun renderOffset(): FloatArray? = null
	override fun renderRotation(): Float? = null
}