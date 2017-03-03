package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.XmlReader

class SpinAnimation : AbstractRotationAnimation()
{
	var duration = 0f
	var time = 0f
	var targetRotation = 0f
	var currentRotation = 0f

	override fun renderRotation(): Float? = currentRotation
	override fun duration(): Float = duration
	override fun time(): Float = time

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = time / duration
		currentRotation = targetRotation * alpha

		return time >= duration
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	fun set(duration: Float, rotation: Float): SpinAnimation
	{
		this.duration = duration
		this.targetRotation = rotation

		this.time = 0f
		this.currentRotation = 0f

		return this
	}

	override fun copy(): AbstractAnimation = obtain().set(duration, targetRotation)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<SpinAnimation> = object : Pool<SpinAnimation>() {
			override fun newObject(): SpinAnimation
			{
				return SpinAnimation()
			}

		}

		@JvmStatic fun obtain(): SpinAnimation
		{
			val anim = pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { pool.free(this); obtained = false } }
}