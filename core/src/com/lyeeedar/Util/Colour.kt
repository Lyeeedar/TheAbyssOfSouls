package com.lyeeedar.Util

import com.badlogic.gdx.graphics.Color

/**
 * Created by Philip on 30-Mar-16.
 */

class Colour()
{
	@JvmField var r: Float = 0f
	@JvmField var g: Float = 0f
	@JvmField var b: Float = 0f
	@JvmField var a: Float = 0f

	constructor(col: Float) : this()
	{
		set(col)
	}

	constructor(r: Float, g:Float, b:Float, a:Float) : this()
	{
		set(r, g, b, a)
	}

	fun set(other: Colour): Colour
	{
		r = other.r
		g = other.g
		b = other.b
		a = other.a

		return this
	}

	fun set(col: Float): Colour
	{
		r = col
		g = col
		b = col
		a = col

		return this
	}

	fun set(r: Float, g:Float, b:Float, a:Float): Colour
	{
		this.r = r
		this.g = g
		this.b = b
		this.a = a

		return this
	}

	operator fun timesAssign(other: Colour)
	{
		r *= other.r;
		g *= other.g;
		b *= other.b;
		a *= other.a;
	}

	operator fun timesAssign(other: Color)
	{
		r *= other.r;
		g *= other.g;
		b *= other.b;
		a *= other.a;
	}

	operator fun timesAssign(alpha: Float)
	{
		r *= alpha;
		g *= alpha;
		b *= alpha;
		a *= alpha;
	}

	operator fun plusAssign(other: Colour)
	{
		r += other.r;
		g += other.g;
		b += other.b;
		a += other.a;
	}
}