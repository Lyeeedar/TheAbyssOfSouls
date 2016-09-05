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
	private val scale = FloatArray(2)

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderScale(): FloatArray = scale

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)

		scale[0] = alpha
		scale[1] = alpha

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