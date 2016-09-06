package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Colour

/**
 * Created by Philip on 14-Aug-16.
 */

abstract class Timeline<T>
{
	val streams = Array<Array<Pair<Float, T>>>()

	fun length() = streams[0].last().first

	operator fun set(stream: Int, time: Float, value: T)
	{
		if (stream >= streams.size)
		{
			streams.add(Array())
		}
		val keyframes = streams[stream]

		keyframes.add(Pair(time, value))
		keyframes.sort { p1, p2 -> p1.first.compareTo(p2.first) }
	}

	fun valAt(stream: Int, time: Float): T
	{
		val keyframes = streams[stream]

		if (keyframes.size == 1) return keyframes[0].second

		var prev: Pair<Float, T> = keyframes[0]
		var next: Pair<Float, T> = keyframes[0]
		for (i in 1..keyframes.size-1)
		{
			prev = next
			next = keyframes[i]

			if (prev.first <= time && next.first >= time) break
		}

		val alpha = (time - prev.first) / (next.first - prev.first)
		val out = lerpValue(prev.second, next.second, alpha)
		return out
	}

	abstract fun lerpValue(prev: T, next: T, alpha: Float): T

	fun parse(xml: XmlReader.Element, createFunc: (String) -> T, timeScale: Float = 1f)
	{
		val streamsEl = xml.getChildrenByName("Stream")
		if (streamsEl.size == 0)
		{
			parseStream(xml, 0, createFunc, timeScale)
		}
		else
		{
			for (i in 0..streamsEl.size-1)
			{
				val el = streamsEl[i]
				parseStream(el, i, createFunc, timeScale)
			}
		}
	}

	fun parseStream(xml: XmlReader.Element, stream: Int, createFunc: (String) -> T, timeScale: Float = 1f)
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)

			if (el.text != null)
			{
				val split = el.text.split("|")
				val time = split[0].toFloat()
				val rest = el.text.replaceFirst(split[0]+"|", "")
				val value = createFunc(rest)
				this[stream, time * timeScale] = value
			}
			else
			{
				val time = el.getFloat("Time")
				val sval = el.get("Value")
				val value = createFunc(sval)
				this[stream, time * timeScale] = value
			}
		}
	}
}

class StepTimeline<T> : Timeline<T>()
{
	override fun lerpValue(prev: T, next: T, alpha: Float): T = prev
}

class LerpTimeline : Timeline<Float>()
{
	override fun lerpValue(prev: Float, next: Float, alpha: Float): Float = Interpolation.linear.apply(prev, next, alpha)
}

data class Range(var v1: Float, var v2: Float)
{
	constructor(asString: String)
		: this(0f, 0f)
	{
		val seperator = if (asString.contains("|")) "|" else ","

		val split = asString.split(seperator)
		v1 = split[0].toFloat()
		v2 = split[1].toFloat()
	}

	fun lerp(alpha: Float) = Interpolation.linear.apply(v1, v2, alpha)
}

class RangeLerpTimeline : Timeline<Range>()
{
	val temp = Range(0f, 0f)
	override fun lerpValue(prev: Range, next: Range, alpha: Float): Range
	{
		temp.v1 = Interpolation.linear.apply(prev.v1, next.v1, alpha)
		temp.v2 = Interpolation.linear.apply(prev.v2, next.v2, alpha)

		return temp
	}
}

class ColourTimeline : Timeline<Colour>()
{
	val temp = Colour()
	override fun lerpValue(prev: Colour, next: Colour, alpha: Float): Colour = temp.set(prev).lerp(next, alpha)
}

class VectorTimeline: Timeline<Vector2>()
{
	val temp = Vector2()
	override fun lerpValue(prev: Vector2, next: Vector2, alpha: Float): Vector2 = temp.set(prev).lerp(next, alpha)
}