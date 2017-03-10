package com.lyeeedar.Util

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Pools

fun max(vararg floats: Float): Float = floats.max()!!
fun min(vararg floats: Float): Float = floats.min()!!

class Smoothstep() : Interpolation()
{
	override fun apply(a: Float): Float = a * a * ( 3f - 2f * a )
}
val smoothStep = Smoothstep()

class Leap() : Interpolation()
{
	override fun apply(a: Float): Float
	{
		var t = a

		if (t <= 0.5f) return 2.0f * t * (1.0f - t)

		t -= 0.5f

		return 2.0f * t * t + 0.5f
	}
}
val leap = Leap()

fun vectorToAngle(x: Float, y: Float) : Float
{
	// basis vector 0,1
	val dot = (0 * x + 1 * y).toDouble() // dot product
	val det = (0 * y - 1 * x).toDouble() // determinant
	val angle = Math.atan2(det, dot).toFloat() * MathUtils.radiansToDegrees

	return angle
}

fun getRotation(p1: Point, p2: Point) : Float
{
	val vec = Pools.obtain(Vector2::class.java)
	vec.x = (p2.x - p1.x).toFloat()
	vec.y = (p2.y - p1.y).toFloat()
	vec.nor()

	val angle = vectorToAngle(vec.x, vec.y)

	Pools.free(vec)

	return angle
}

fun getRotation(p1: Vector2, p2: Vector2) : Float
{
	val vec = Pools.obtain(Vector2::class.java)
	vec.x = (p2.x - p1.x).toFloat()
	vec.y = (p2.y - p1.y).toFloat()
	vec.nor()

	val angle = vectorToAngle(vec.x, vec.y)

	Pools.free(vec)

	return angle
}

fun Int.abs() = Math.abs(this)

fun Float.abs() = Math.abs(this)
fun Float.ciel() = MathUtils.ceil(this)
fun Float.floor() = MathUtils.floor(this)
fun Float.round() = MathUtils.round(this)
fun Float.clamp(min: Float, max: Float) = MathUtils.clamp(this, min , max)

fun Float.lerp(target: Float, alpha: Float) = MathUtils.lerp(this, target, alpha)

fun Int.romanNumerals(): String
{
	return when (this)
	{
		1 -> "I"
		2 -> "II"
		3 -> "III"
		4 -> "IV"
		5 -> "V"
		6 -> "VI"
		7 -> "VII"
		8 -> "VIII"
		9 -> "IX"
		10 -> "X"
		11 -> "XI"
		12 -> "XII"
		13 -> "XIII"
		14 -> "XIV"
		15 -> "XV"
		16 -> "XVI"
		17 -> "XVII"
		18 -> "XVIII"
		19 -> "XIX"
		20 -> "XX"
		else -> "--"
	}
}