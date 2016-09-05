package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 28-Apr-16.
 */

class HybridAnimation(): AbstractAnimation()
{
	var offset: AbstractMoveAnimation? = null
	var scale: AbstractScaleAnimation? = null
	var colour: AbstractColourAnimation? = null

	override fun duration(): Float = Math.max(Math.max(offset?.duration() ?: 0f, scale?.duration() ?: 0f), colour?.duration() ?: 0f)
	override fun time(): Float = Math.min(Math.min(offset?.time() ?: duration(), scale?.time() ?: duration()), colour?.time() ?: duration())

	override fun renderOffset(): FloatArray? = offset?.renderOffset() ?: scale?.renderOffset() ?: colour?.renderOffset()
	override fun renderScale(): FloatArray? = scale?.renderScale() ?: offset?.renderScale() ?: colour?.renderScale()
	override fun renderColour(): Colour? = colour?.renderColour() ?: offset?.renderColour() ?: scale?.renderColour()

	override fun update(delta: Float): Boolean
	{
		if (offset?.update(delta) ?: false) { offset?.free(); offset = null }
		if (scale?.update(delta) ?: false) { scale?.free(); scale = null }
		if (colour?.update(delta) ?: false) { colour?.free(); colour = null }

		return offset == null && scale == null && colour == null
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun free()
	{
		offset?.free()
		offset = null

		scale?.free()
		scale = null

		colour?.free()
		colour = null
	}

	override fun copy(): AbstractAnimation
	{
		val anim = HybridAnimation()

		anim.offset = offset?.copy() as? AbstractMoveAnimation
		anim.scale = scale?.copy() as? AbstractScaleAnimation
		anim.colour = colour?.copy() as? AbstractColourAnimation

		return anim
	}
}
