package com.lyeeedar.Util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

/**
 * Created by Philip on 01-Aug-16.
 */

class HSLColour
{
	private val tempVec = Vector3()

	/** Hue  */
	var h: Float = 0.toFloat()
	/** Saturation  */
	var s: Float = 0.toFloat()
	/** Lighting  */
	var l: Float = 0.toFloat()

	private var a: Float = 0.toFloat()

	/**
	 * Default constructor, constructs a HSL with 0.0f h, s, and l.
	 */
	constructor()
		: this(0.0f, 0.0f, 0.0f, 1.0f)
	{

	}

	/**
	 * Construct a color from the RGB color space as HSL.
	 * @param color The RGB color to convert to HSL.
	 */
	constructor(colour: Colour)
	{
		set(colour)
	}

	/**
	 * Constructs a color in the HSL color space.
	 * @param h Hue
	 * @param s Saturation
	 * @param l Lighting
	 * @param a Alpha
	 */
	constructor(h: Float, s: Float, l: Float, a: Float)
	{
		set(h, s, l, a)
	}

	fun set(colour: HSLColour): HSLColour = set(colour.h, colour.s, colour.l, colour.a)

	fun set(colour: Colour): HSLColour
	{
		val hslVec = rgbToHsl(colour)
		return set(hslVec.x, hslVec.y, hslVec.z, colour.a)
	}

	fun set(hue: Float, saturation: Float, lightness: Float, alpha: Float): HSLColour
	{
		this.h = hue
		this.s = saturation
		this.l = lightness
		this.a = alpha

		return this
	}

	fun lerp(target: HSLColour, t: Float): HSLColour
	{
		this.h += t * (target.h - this.h)
		this.s += t * (target.s - this.s)
		this.l += t * (target.l - this.l)
		this.a += t * (target.a - this.a)
		return this
	}

	/**
	 * Converts an HSL color value to RGB. Conversion formula
	 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
	 * Assumes h, s, and l are contained in the set [0, 1] and
	 * returns r, g, and b in the set [0, 1].

	 * @return The RGB representation
	 */
	fun toRGB(color: Colour? = null): Colour
	{
		val r: Float
		val g: Float
		val b: Float

		if (s == 0f)
		{
			r = l
			g = l
			b = l
		}
		else
		{
			val q = if (l < 0.5f) l * (1.0f + s) else l + s - l * s
			val p = 2.0f * l - q
			r = hue2rgb(p, q, h + 1.0f / 3.0f)
			g = hue2rgb(p, q, h)
			b = hue2rgb(p, q, h - 1.0f / 3.0f)
		}

		val col = color ?: Colour()

		return col.set(r, g, b, a)
	}

	private fun hue2rgb(p: Float, q: Float, t: Float): Float
	{
		var t = t
		if (t < 0.0f) t += 1.0f
		if (t > 1.0f) t -= 1.0f
		if (t < 1.0f / 6.0f) return p + (q - p) * 6.0f * t
		if (t < 1.0f / 2.0f) return q
		if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6.0f
		return p
	}

	/**
	 * Converts an RGB color value to HSL. Conversion formula
	 * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
	 * Assumes r, g, and b are contained in the set [0, 1] and
	 * returns h, s, and l in the set [0, 1].

	 * @param rgba the could value of the
	 * *
	 * @return           The HSL representation
	 */
	private fun rgbToHsl(rgba: Colour): Vector3
	{
		val r = rgba.r
		val g = rgba.g
		val b = rgba.b

		val max = if (r > g && r > b) r else if (g > b) g else b
		val min = if (r < g && r < b) r else if (g < b) g else b

		var h: Float = (max + min) / 2.0f
		var s: Float = h
		val l: Float = h

		if (max == min)
		{
			h = 0.0f
			s = 0.0f
		}
		else
		{
			val d = max - min
			s = if (l > 0.5f) d / (2.0f - max - min) else d / (max + min)

			if (r > g && r > b)
				h = (g - b) / d + (if (g < b) 6.0f else 0.0f)
			else if (g > b)
				h = (b - r) / d + 2.0f
			else
				h = (r - g) / d + 4.0f

			h /= 6.0f
		}

		return tempVec.set(h, s, l)
	}
}