package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 04-Aug-16.
 */

class ExtendAnimation() : AbstractScaleAnimation()
{
	private var duration: Float = 0f
	private var time: Float = 0f
	private val offset = floatArrayOf(0f, 0f)
	private val scale = floatArrayOf(1f, 1f)
	private val diff = floatArrayOf(0f, 0f)
	private var finalScale: Float = 0f

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderScale(): FloatArray = scale
	override fun renderOffset(): FloatArray = offset

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)

		offset[0] = ( diff[0] / 2f ) * alpha
		offset[1] = ( diff[1] / 2f ) * alpha

		scale[1] = 1f + finalScale * alpha

		if (time >= duration)
		{
			return true
		}

		return false
	}

	operator fun set(duration: Float, diff: FloatArray, finalScale: Float): ExtendAnimation
	{
		this.duration = duration
		this.time = 0f
		this.diff[0] = diff[0]
		this.diff[1] = diff[1]
		this.finalScale = finalScale
		this.time = 0f

		update(0f)

		return this
	}

	operator fun set(duration: Float, diff: Array<Vector2>): ExtendAnimation
	{
		this.duration = duration
		this.time = 0f
		this.diff[0] = diff.last().x - diff.first().x
		this.diff[1] = diff.last().y - diff.first().y
		this.time = 0f

		val dist = diff.first().dst(diff.last()) + 2f
		finalScale = dist / 2f

		update(0f)

		return this
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun copy(): AbstractAnimation = ExtendAnimation.obtain().set(duration, diff, finalScale)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ExtendAnimation> = object : Pool<ExtendAnimation>() {
			override fun newObject(): ExtendAnimation
			{
				return ExtendAnimation()
			}

		}

		@JvmStatic fun obtain(): ExtendAnimation
		{
			val anim = ExtendAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { ExtendAnimation.pool.free(this); obtained = false } }

}