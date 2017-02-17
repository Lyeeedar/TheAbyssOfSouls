package com.lyeeedar.Util

/**
 * Created by Philip on 13-Jul-16.
 */

class Event0Arg {
	private val handlers = arrayListOf<(() -> Unit)>()
	operator fun plusAssign(handler: () -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: () -> Unit) { handlers.remove(handler) }
	operator fun invoke() { for (handler in handlers) handler() }
}

class Event1Arg<T> {
	private val handlers = arrayListOf<((T) -> Unit)>()
	operator fun plusAssign(handler: (T) -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: (T) -> Unit) { handlers.remove(handler) }
	operator fun invoke(value: T) { for (handler in handlers) handler(value) }
}

class Event2Arg<T1, T2> {
	private val handlers = arrayListOf<((T1, T2) -> Unit)>()
	operator fun plusAssign(handler: (T1, T2) -> Unit) { handlers.add(handler) }
	operator fun minusAssign(handler: (T1, T2) -> Unit) { handlers.remove(handler) }
	operator fun invoke(value1: T1, value2: T2) { for (handler in handlers) handler(value1, value2) }
}