package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.GlobalData

class StretchAnimation : AbstractSpriteAnimation
{
	enum class StretchEquation
	{
		EXTEND, REVERSEEXTEND, EXPAND
	}

	private var diff: FloatArray? = null
	private var finalScale: Float = 0.toFloat()
	private var eqn: StretchEquation? = null
	private var trueDuration: Float = 0.toFloat()
	private var animSpeed = 0.2f
	private var padding = 0.5f

	private val offset = floatArrayOf(0f, 0f)
	private val scale = floatArrayOf(1f, 1f)

	constructor()
	{

	}

	fun set(duration: Float, diff: FloatArray?, padDuration: Float, eqn: StretchEquation): StretchAnimation
	{
		var padDuration = padDuration
		padDuration *= GlobalData.Global.animationSpeed

		this.duration = duration + padDuration
		this.duration *= GlobalData.Global.animationSpeed


		this.diff = diff
		this.eqn = eqn
		this.trueDuration = duration

		if (diff != null)
		{
			val dist = Math.sqrt((diff[0] * diff[0] + diff[1] * diff[1]).toDouble()).toFloat() + GlobalData.Global.tileSize * 2
			finalScale = dist / GlobalData.Global.tileSize / 2.0f
		}

		time = 0f

		return this
	}

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp((trueDuration - time) / trueDuration, 0f, 1f)

		if (eqn == StretchEquation.EXTEND)
		{
			offset[0] = (diff!![0] / 2 + diff!![0] / 2 * alpha).toInt().toFloat()
			offset[1] = (diff!![1] / 2 + diff!![1] / 2 * alpha).toInt().toFloat()

			scale[1] = 1 + finalScale * (1 - alpha)
		} else if (eqn == StretchEquation.REVERSEEXTEND)
		{
			offset[0] = (diff!![0] / 2 * (1 - alpha)).toInt().toFloat()
			offset[1] = (diff!![1] / 2 * (1 - alpha)).toInt().toFloat()

			scale[1] = 1 + finalScale * (1 - alpha)
		} else if (eqn == StretchEquation.EXPAND)
		{
			scale[0] = 1 - alpha
			scale[1] = 1 - alpha
		}

		return time > duration
	}

	override fun getRenderOffset(): FloatArray
	{
		return offset
	}

	override fun getRenderScale(): FloatArray
	{
		return scale
	}

	override fun set(duration: Float, diff: FloatArray)
	{
		var duration = duration
		duration = (duration * animSpeed)
		this.duration = duration + padding
		this.trueDuration = duration
		this.diff = diff
		this.time = 0f

		val dist = Math.sqrt((diff[0] * diff[0] + diff[1] * diff[1]).toDouble()).toFloat() + GlobalData.Global.tileSize * 2
		finalScale = dist / GlobalData.Global.tileSize / 2.0f
	}

	override fun copy(): AbstractSpriteAnimation
	{
		val anim = StretchAnimation()
		anim.duration = duration
		anim.trueDuration = trueDuration
		anim.diff = diff
		anim.finalScale = finalScale
		anim.eqn = eqn

		return anim
	}

	override fun parse(xml: Element)
	{
		eqn = StretchEquation.valueOf(xml.get("Equation", "Extend").toUpperCase())
		animSpeed = xml.getFloat("AnimationSpeed", 0.2f)
		padding = xml.getFloat("Padding", 0.5f)
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<StretchAnimation> = Pools.get( StretchAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): StretchAnimation
		{
			val anim = StretchAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { StretchAnimation.pool.free(this); obtained = false } }
}