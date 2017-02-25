package com.lyeeedar.Renderables

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.lyeeedar.Renderables.Animation.*


abstract class Renderable
{
	var batchID: Int = 0

	var visible = true
	var renderDelay = -1f
	var showBeforeRender = false

	val size = intArrayOf(1, 1)

	var rotation: Float = 0f

	var flipX: Boolean = false
	var flipY: Boolean = false

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
						hybrid.offsets.add(anim)
					}
					else if (anim is AbstractScaleAnimation)
					{
						hybrid.scales.add(anim)
					}
					else if (anim is AbstractColourAnimation)
					{
						hybrid.colours.add(anim)
					}
					else if (anim is AbstractRotationAnimation)
					{
						hybrid.rotations.add(anim)
					}
					else if (anim is HybridAnimation)
					{
						hybrid.offsets.addAll(anim.offsets)
						hybrid.scales.addAll(anim.scales)
						hybrid.colours.addAll(anim.colours)
						hybrid.rotations.addAll(anim.rotations)
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

	fun render(batch: Batch, x: Float, y: Float, tileSize: Float)
	{
		if (!visible) return
		if (renderDelay > 0 && !showBeforeRender)
		{
			return
		}

		doRender(batch, x, y, tileSize)
	}

	abstract fun doUpdate(delta: Float): Boolean
	abstract fun doRender(batch: Batch, x: Float, y: Float, tileSize: Float)

	abstract fun copy(): Renderable
}