package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.HSLColour

/**
 * Created by Philip on 31-Jul-16.
 */

class ChromaticAnimation() : AbstractColourAnimation()
{
	private var duration: Float = 0f
	private var time: Float = 0f
	private val colour: Colour = Colour()
	private val hsl: HSLColour = HSLColour()

	override fun duration(): Float = duration
	override fun time(): Float = time
	override fun renderColour(): Colour = colour

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)

		hsl.set(alpha, 1f, 0.5f, 1f)
		hsl.toRGB(colour)

		if (time >= duration)
		{
			time -= duration
		}

		return false
	}

	fun set(duration: Float): ChromaticAnimation
	{
		this.duration = duration
		this.oneTime = false

		this.time = 0f

		return this
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun copy(): AbstractAnimation = ChromaticAnimation.obtain().set(duration)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ChromaticAnimation> = object : Pool<ChromaticAnimation>() {
			override fun newObject(): ChromaticAnimation
			{
				return ChromaticAnimation()
			}

		}

		@JvmStatic fun obtain(): ChromaticAnimation
		{
			val anim = ChromaticAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { ChromaticAnimation.pool.free(this); obtained = false } }
}