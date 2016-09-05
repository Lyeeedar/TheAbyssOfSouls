package com.lyeeedar.Util

/**
 * Created by Philip on 13-Jul-16.
 */

class Event0Arg {
	private val handlers = arrayListOf<(Event0Arg.() -> Unit)>()
	operator fun plusAssign(handler: Event0Arg.() -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: Event0Arg.() -> Unit) { handlers.remove(handler) }
	operator fun invoke() { for (handler in handlers) handler() }
}

class Event1Arg<T> {
	private val handlers = arrayListOf<(Event1Arg<T>.(T) -> Unit)>()
	operator fun plusAssign(handler: Event1Arg<T>.(T) -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: Event1Arg<T>.(T) -> Unit) { handlers.remove(handler) }
	operator fun invoke(value: T) { for (handler in handlers) handler(value) }
}

class Event2Arg<T1, T2> {
	private val handlers = arrayListOf<(Event2Arg<T1, T2>.(T1, T2) -> Unit)>()
	operator fun plusAssign(handler: Event2Arg<T1, T2>.(T1, T2) -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: Event2Arg<T1, T2>.(T1, T2) -> Unit) { handlers.remove(handler) }
	operator fun invoke(value1: T1, value2: T2) { for (handler in handlers) handler(value1, value2) }
}