package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 31-Jul-16.
 */

class AlphaAnimation() : AbstractColourAnimation()
{
	private var duration: Float = 0f
	private var time: Float = 0f
	private val colour: Colour = Colour()
	private val startColour: Colour = Colour()
	private lateinit var alphas: FloatArray

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderColour(): Colour = colour

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)
		val index = MathUtils.clamp(Math.round(alphas.size.toFloat() * alpha).toInt(), 0, alphas.size-1)

		colour.set(startColour)
		colour.a = alphas[index]

		if (time >= duration)
		{
			if (oneTime) return true
			else time -= duration
		}

		return false
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	fun set(alphas: FloatArray, start: Colour, duration: Float, oneTime: Boolean = true): AlphaAnimation
	{
		this.startColour.set(start)
		this.alphas = alphas
		this.duration = duration
		this.oneTime = oneTime

		this.time = 0f
		this.colour.set(start)

		return this
	}

	override fun copy(): AbstractAnimation = AlphaAnimation.obtain().set(alphas, startColour, duration, oneTime)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<AlphaAnimation> = Pools.get( AlphaAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): AlphaAnimation
		{
			val anim = AlphaAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { AlphaAnimation.pool.free(this); obtained = false } }
}