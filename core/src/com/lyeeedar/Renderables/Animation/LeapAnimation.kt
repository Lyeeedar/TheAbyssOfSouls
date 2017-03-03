package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Util.Point

class LeapAnimation : AbstractMoveAnimation
{
	override fun duration(): Float = duration

	override fun time(): Float = time

	override fun renderOffset(): FloatArray? = offset

	private val offset = floatArrayOf(0f, 0f)
	private var duration: Float = 0f
	private var time: Float = 0f

	private val p1: Vector2 = Vector2()
	private val p2: Vector2 = Vector2()
	private val temp = Vector2()
	private var height: Float = 1f

	constructor()
	{

	}

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(time / duration, 0f, 1f)
		val lalpha = (alpha - 0.5f) / 0.5f
		val halpha = Math.sqrt((1 - lalpha * lalpha).toDouble()).toFloat()

		temp.set(p1).lerp(p2, alpha)

		offset[0] = temp.x
		offset[1] = temp.y + height * halpha

		return time >= duration
	}

	fun set(duration: Float, p1: Vector2, p2: Vector2, height: Float): LeapAnimation
	{
		this.duration = duration
		this.p1.set(p1)
		this.p2.set(p2)
		this.height = height
		this.time = 0f

		update(0f)

		return this
	}

	fun setRelative(duration: Float, p1: Point, p2: Point, height: Float): LeapAnimation
	{
		this.duration = duration
		this.p1.set(p1.x.toFloat() - p2.x.toFloat(), p1.y.toFloat() - p2.y.toFloat())
		this.p2.set(0f, 0f)
		this.height = height
		this.time = 0f

		update(0f)

		return this
	}

	fun setAbsolute(duration: Float, p1: Point, p2: Point, height: Float): LeapAnimation
	{
		this.duration = duration
		this.p1.set(p1.x.toFloat(), p1.y.toFloat())
		this.p2.set(p2.x.toFloat(), p2.y.toFloat())
		this.height = height
		this.time = 0f

		update(0f)

		return this
	}

	fun set(duration: Float, path: Array<Vector2>, height: Float): LeapAnimation
	{
		this.duration = duration
		this.p1.set(path.first())
		this.p2.set(path.last())
		this.height = height
		this.time = 0f

		update(0f)

		return this
	}

	override fun parse(xml: Element)
	{
	}

	override fun copy(): AbstractAnimation = obtain().set(duration, p1, p2, height)

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<LeapAnimation> = object : Pool<LeapAnimation>() {
			override fun newObject(): LeapAnimation
			{
				return LeapAnimation()
			}

		}

		@JvmStatic fun obtain(): LeapAnimation
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
