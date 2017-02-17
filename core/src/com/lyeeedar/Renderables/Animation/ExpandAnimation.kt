package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 31-Jul-16.
 */

class ExpandAnimation() : AbstractScaleAnimation()
{
	private var duration: Float = 0f
	private var time: Float = 0f

	private var startScale = FloatArray(2)
	private var endScale = FloatArray(2)

	private val scale = FloatArray(2)
	private var oneway = true

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderScale(): FloatArray = scale

	override fun update(delta: Float): Boolean
	{
		time += delta

		var alpha = MathUtils.clamp(time / duration, 0f, 1f)

		if (!oneway)
		{
			alpha = (alpha - 0.5f) / 0.5f
			alpha = Math.sqrt((1 - alpha * alpha).toDouble()).toFloat()
		}

		scale[0] = startScale[0] + alpha * (endScale[0] - startScale[0])
		scale[1] = startScale[1] + alpha * (endScale[1] - startScale[1])

		if (time >= duration)
		{
			return true
		}

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	fun set(duration: Float): ExpandAnimation
	{
		startScale[0] = 0f
		startScale[1] = 0f

		endScale[0] = 1f
		endScale[1] = 1f

		oneway = true

		this.duration = duration
		this.time = 0f
		return this
	}

	fun set(duration: Float, start: Float, end: Float, oneway: Boolean = true): ExpandAnimation
	{
		startScale[0] = start
		startScale[1] = start

		endScale[0] = end
		endScale[1] = end

		this.oneway = oneway

		this.duration = duration
		this.time = 0f
		return this
	}

	override fun copy(): AbstractAnimation = ExpandAnimation.obtain().set(duration)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ExpandAnimation> = Pools.get( ExpandAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): ExpandAnimation
		{
			val anim = ExpandAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { ExpandAnimation.pool.free(this); obtained = false } }
}