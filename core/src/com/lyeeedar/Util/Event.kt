package com.lyeeedar.Util

import com.badlogic.gdx.utils.Array

/**
 * Created by Philip on 13-Jul-16.
 */

class Event0Arg {
	private val handlers = Array<(() -> Boolean)>(false, 4)

	operator fun plusAssign(handler: () -> Boolean)
	{
		if (invoking) throw Exception("Cannot add handlers during invoke!")
		handlers.add(handler)
	}

	operator fun minusAssign(handler: () -> Boolean)
	{
		if (invoking) throw Exception("Cannot remove handlers during invoke!")
		handlers.removeValue(handler, true)
	}

	var invoking = false
	operator fun invoke()
	{
		invoking = true

		val itr = handlers.iterator()
		while (itr.hasNext())
		{
			val handler = itr.next()
			if (handler.invoke()) itr.remove()
		}

		invoking = false
	}
}

class Event1Arg<T> {
	private val handlers = Array<((T) -> Boolean)>(false, 4)

	operator fun plusAssign(handler: (T) -> Boolean)
	{
		if (invoking) throw Exception("Cannot add handlers during invoke!")
		handlers.add(handler)
	}

	operator fun minusAssign(handler: (T) -> Boolean)
	{
		if (invoking) throw Exception("Cannot remove handlers during invoke!")
		handlers.removeValue(handler, true)
	}

	var invoking = false
	operator fun invoke(value: T)
	{
		invoking = true

		val itr = handlers.iterator()
		while (itr.hasNext())
		{
			val handler = itr.next()
			if (handler.invoke(value)) itr.remove()
		}

		invoking = false
	}
}

class Event2Arg<T1, T2> {
	private val handlers = Array<((T1, T2) -> Boolean)>(false, 4)

	operator fun plusAssign(handler: (T1, T2) -> Boolean)
	{
		if (invoking) throw Exception("Cannot add handlers during invoke!")
		handlers.add(handler)
	}

	operator fun minusAssign(handler: (T1, T2) -> Boolean)
	{
		if (invoking) throw Exception("Cannot remove handlers during invoke!")
		handlers.removeValue(handler, true)
	}

	var invoking = false
	operator fun invoke(value1: T1, value2: T2)
	{
		invoking = true

		val itr = handlers.iterator()
		while (itr.hasNext())
		{
			val handler = itr.next()
			if (handler.invoke(value1, value2)) itr.remove()
		}

		invoking = false
	}
}