package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.GlobalData
import com.lyeeedar.Util.Point

class MoveAnimation : AbstractSpriteAnimation
{
	enum class MoveEquation
	{
		LINEAR, SMOOTHSTEP, EXPONENTIAL, LEAP
	}

	private var diff: FloatArray? = null
	private var eqn: MoveEquation? = null

	var leapHeight = 3f

	private val offset = floatArrayOf(0f, 0f)

	constructor()
	{

	}

	fun set(duration: Float, diff: FloatArray, eqn: MoveEquation): MoveAnimation
	{
		this.duration = duration
		this.diff = diff
		this.eqn = eqn

		time = 0f

		return this
	}

	override fun update(delta: Float): Boolean
	{
		time += delta

		var alpha = MathUtils.clamp((duration - time) / duration, 0f, 1f)

		if (eqn == MoveEquation.SMOOTHSTEP)
		{
			alpha = (alpha * alpha * (3 - 2 * alpha)) // smoothstep
		}
		else if (eqn == MoveEquation.EXPONENTIAL)
		{
			alpha = 1 - (1 - alpha) * (1 - alpha) * (1 - alpha) * (1 - alpha)
		}

		offset[0] = (diff!![0] * alpha).toInt().toFloat()
		offset[1] = (diff!![1] * alpha).toInt().toFloat()

		if (eqn == MoveEquation.LEAP)
		{
			// B2(t) = (1 - t) * (1 - t) * p0 + 2 * (1-t) * t * p1 + t*t*p2
			alpha = (1 - alpha) * (1 - alpha) * 0f + 2f * (1 - alpha) * alpha * 1f + alpha * alpha * 0f
			offset[1] += GlobalData.Global.tileSize * leapHeight * alpha
		}

		return time > duration
	}

	override fun getRenderOffset(): FloatArray
	{
		return offset
	}

	override fun getRenderScale(): FloatArray?
	{
		return null
	}

	override fun set(duration: Float, diff: FloatArray)
	{
		this.duration = duration
		this.time = 0f
		this.diff = diff
	}

	override fun parse(xml: Element)
	{
		eqn = MoveEquation.valueOf(xml.get("Equation", "SmoothStep").toUpperCase())
		leapHeight = xml.getFloat("LeapHeight", leapHeight)
	}

	override fun copy(): AbstractSpriteAnimation
	{
		val anim = MoveAnimation()
		anim.eqn = eqn
		anim.leapHeight = leapHeight
		anim.duration = duration
		anim.diff = diff

		return anim
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<MoveAnimation> = Pools.get( MoveAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): MoveAnimation
		{
			val anim = MoveAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { MoveAnimation.pool.free(this); obtained = false } }
}
