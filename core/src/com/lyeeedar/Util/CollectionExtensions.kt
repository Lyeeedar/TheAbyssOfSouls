package com.lyeeedar.Util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import squidpony.squidmath.LightRNG

fun <T> com.badlogic.gdx.utils.Array<T>.tryGet(i: Int): T = this[MathUtils.clamp(i, 0, this.size-1)]
fun <T> com.badlogic.gdx.utils.Array<T>.random(ran: LightRNG): T = this[ran.nextInt(this.size)]
fun <T> com.badlogic.gdx.utils.Array<T>.removeRandom(ran: LightRNG): T
{
	val index = ran.nextInt(this.size)
	val item = this[index]
	this.removeIndex(index)

	return item
}
fun <T> com.badlogic.gdx.utils.Array<T>.addAll(collection: Sequence<T>)
{
	for (item in collection) this.add(item)
}
fun <T> Iterable<T>.asGdxArray(): com.badlogic.gdx.utils.Array<T> {
	val array = Array<T>()
	array.addAll(this.asSequence())
	return array
}

fun <T> Sequence<T>.random() = if (this.count() > 0) this.elementAt(Random.random(this.count()-1)) else null
fun <T> Sequence<T>.random(ran: LightRNG) = if (this.count() > 0) this.elementAt(ran.nextInt(this.count())) else null
inline fun <reified T> Sequence<T>.random(num: Int): Sequence<T>
{
	val array = Array<T>(this.count())
	for (item in this) array.add(item)

	val outArray = Array<T>()
	for (i in 0..num-1)
	{
		if (array.size == 0) break
		outArray.add(array.removeRandom(Random.random))
	}

	return outArray.asSequence()
}
inline fun <reified T> Sequence<T>.random(num: Int, ran: LightRNG): Sequence<T>
{
	val array = Array<T>(this.count())
	for (item in this) array.add(item)

	val outArray = Array<T>()
	for (i in 0..num-1)
	{
		if (array.size == 0) break
		outArray.add(array.removeRandom(ran))
	}

	return outArray.asSequence()
}
inline fun <reified T> Sequence<T>.weightedRandom(weightFun: (T) -> Int, ran: LightRNG = Random.random): T?
{
	if (this.count() == 0) return null

	val totalWeight = this.sumBy { weightFun(it) }

	if (totalWeight == 0) return null

	val chosen = ran.nextInt(totalWeight)

	var current = 0
	for (e in this)
	{
		val weight = weightFun(e)
		if (weight > 0 && chosen - current < weight)
		{
			return e
		}

		current += weight
	}

	return lastOrNull()
}

fun <T> Sequence<T>.sequenceEquals(other: Sequence<T>): Boolean
{
	if (this.count() != other.count()) return false

	for (item in this)
	{
		if (!other.contains(item)) return false
	}

	for (item in other)
	{
		if (!this.contains(item)) return false
	}

	return true
}
