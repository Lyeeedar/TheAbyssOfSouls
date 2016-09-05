package com.lyeeedar.Renderables

import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Renderables.Animation.*


abstract class Renderable
{
	var batchID: Int = 0

	var visible = true
	var renderDelay = -1f
	var showBeforeRender = false

	var animation: AbstractAnimation? = null
		set(value)
		{
			if (value != null && field != null)
			{
				val hybrid = field as? HybridAnimation ?: HybridAnimation()

				fun merge(anim: AbstractAnimation)
				{
					if (anim is AbstractMoveAnimation)
					{
						hybrid.offset = anim
					}
					else if (anim is AbstractScaleAnimation)
					{
						hybrid.scale = anim
					}
					else if (anim is AbstractColourAnimation)
					{
						hybrid.colour = anim
					}
					else if (anim is HybridAnimation)
					{
						hybrid.offset = anim.offset
						hybrid.scale = anim.scale
						hybrid.colour = anim.colour
					}
					else throw RuntimeException("No entry for anim type '$anim'")
				}

				merge(field!!)
				merge(value)

				field = hybrid
			}
			else
			{
				field = value
			}
		}

	fun update(delta: Float): Boolean
	{
		if (renderDelay > 0)
		{
			renderDelay -= delta

			if (renderDelay > 0)
			{
				return false
			}
		}

		return doUpdate(delta)
	}

	fun render(batch: HDRColourSpriteBatch, x: Float, y: Float, tileSize: Float)
	{
		if (!visible) return
		if (renderDelay > 0 && !showBeforeRender)
		{
			return
		}

		doRender(batch, x, y, tileSize)
	}

	abstract fun doUpdate(delta: Float): Boolean
	abstract fun doRender(batch: HDRColourSpriteBatch, x: Float, y: Float, tileSize: Float)

	abstract fun copy(): Renderable
}