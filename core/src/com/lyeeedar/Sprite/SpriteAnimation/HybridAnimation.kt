package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 28-Apr-16.
 */

class HybridAnimation(): AbstractSpriteAnimation()
{
	var offset: AbstractSpriteAnimation? = null
	var scale: AbstractSpriteAnimation? = null

	override fun set(duration: Float, diff: FloatArray)
	{
		offset?.set(duration, diff)
		scale?.set(duration, diff)
	}

	override fun duration(): Float = Math.max(offset?.duration() ?: 0f, scale?.duration() ?: 0f)
	override fun time(): Float = Math.min(offset?.time() ?: duration(), scale?.time() ?: duration())

	override fun renderOffset(): FloatArray? = offset?.renderOffset()
	override fun renderScale(): FloatArray? = scale?.renderScale()

	override fun update(delta: Float): Boolean
	{
		if (offset?.update(delta) ?: false) { offset?.free(); offset = null }
		if (scale?.update(delta) ?: false) { scale?.free(); scale = null }

		return offset == null && scale == null
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun free()
	{
		offset?.free()
		scale?.free()
	}

	override fun copy(): AbstractSpriteAnimation
	{
		val anim = HybridAnimation()

		anim.offset = offset?.copy()
		anim.scale = scale?.copy()

		return anim
	}
}
