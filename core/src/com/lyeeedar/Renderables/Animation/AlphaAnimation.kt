package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.lerp

/**
 * Created by Philip on 31-Jul-16.
 */

class AlphaAnimation() : AbstractColourAnimation()
{
	private var duration: Float = 0f
	private var time: Float = 0f
	private val colour: Colour = Colour()

	private val startColour: Colour = Colour()
	private var startAlpha: Float = 1f
	private var endAlpha: Float = 1f

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderColour(): Colour = colour

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)
		val alphaValue = startAlpha.lerp(endAlpha, alpha)

		colour.set(startColour)
		colour.a *= alphaValue

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

	fun set(duration: Float, start: Float, end: Float, colour: Colour = Colour.WHITE, oneTime: Boolean = true): AlphaAnimation
	{
		this.startColour.set(colour)
		this.startAlpha = start
		this.endAlpha = end
		this.duration = duration
		this.oneTime = oneTime

		this.time = 0f
		this.colour.set(startColour)

		return this
	}

	override fun copy(): AbstractAnimation = AlphaAnimation.obtain().set(duration, startAlpha, endAlpha, colour, oneTime)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<AlphaAnimation> = object : Pool<AlphaAnimation>() {
			override fun newObject(): AlphaAnimation
			{
				return AlphaAnimation()
			}

		}

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