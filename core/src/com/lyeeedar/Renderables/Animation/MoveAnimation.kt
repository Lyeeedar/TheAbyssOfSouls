package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Path
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.UnsmoothedPath

class MoveAnimation : AbstractMoveAnimation
{
	override fun duration(): Float = duration

	override fun time(): Float = time

	override fun renderOffset(): FloatArray? = offset

	private lateinit var path: Path<Vector2>
	private var eqn: Interpolation? = null

	private var duration: Float = 0f
	private var time: Float = 0f

	private val temp = Vector2()
	private val offset = floatArrayOf(0f, 0f)

	constructor()
	{

	}

	fun set(duration: Float, path: Path<Vector2>, eqn: Interpolation = Interpolation.linear): MoveAnimation
	{
		this.duration = duration
		this.path = path
		this.eqn = eqn

		time = 0f

		update(0f)

		return this
	}

	override fun update(delta: Float): Boolean
	{
		time += delta

		val a = MathUtils.clamp(time / duration, 0f, 1f)

		val alpha = eqn!!.apply(a)
		path.valueAt(temp, alpha)

		offset[0] = temp.x
		offset[1] = temp.y

		return time > duration
	}

	fun set(duration: Float, path: Array<Vector2>, eqn: Interpolation = Interpolation.linear): MoveAnimation
	{
		for (point in path)
		{
			point.x -= path.last().x
			point.y -= path.last().y
		}

		this.duration = duration
		this.time = 0f
		this.path = UnsmoothedPath(path)
		this.eqn = eqn

		time = 0f

		update(0f)

		return this
	}

	fun set(duration: Float, path: Array<Point>, eqn: Interpolation = Interpolation.linear): MoveAnimation
	{
		val vectorPath = com.badlogic.gdx.utils.Array<Vector2>()

		for (point in path)
		{
			point.x -= path.last().x
			point.y -= path.last().y

			vectorPath.add(Vector2(point.x.toFloat(), point.y.toFloat()))
		}

		val asArray = Array<Vector2>(vectorPath.size) { i -> vectorPath[i] }

		this.duration = duration
		this.time = 0f
		this.path = UnsmoothedPath(asArray)
		this.eqn = eqn

		time = 0f

		update(0f)

		return this
	}

	fun set(p1: Point, p2: Point, duration: Float = 0.5f, eqn: Interpolation = Interpolation.linear): MoveAnimation
	{
		val v1 = Vector2((p2.x - p1.x).toFloat(), (p2.y - p1.y).toFloat())

		path = UnsmoothedPath(arrayOf(v1, Vector2()))

		this.duration = duration
		this.time = 0f
		this.eqn = eqn

		time = 0f

		update(0f)

		return this
	}

	override fun parse(xml: Element)
	{
	}

	override fun copy(): AbstractAnimation
	{
		val anim = MoveAnimation.obtain()
		anim.eqn = eqn
		anim.duration = duration
		anim.path = path

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