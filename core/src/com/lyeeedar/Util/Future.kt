package com.lyeeedar.Util

import com.badlogic.gdx.utils.Array

/**
 * Created by Philip on 28-Jul-16.
 */

class Future
{
	companion object
	{
		private val pendingCalls = Array<CallData>(false, 8)

		private val queuedActions = Array<()->Unit>(false, 8)

		private var processing = false

		fun update(delta: Float)
		{
			synchronized(processing)
			{
				if (processing)
					throw Exception("Nested update call!")

				processing = true
			}

			val itr = pendingCalls.iterator()
			while (itr.hasNext())
			{
				val item = itr.next()
				item.delay -= delta

				if (item.delay <= 0f)
				{
					itr.remove()
					item.function.invoke()
				}
			}

			synchronized(processing)
			{
				processing = false

				for (queued in queuedActions)
				{
					queued.invoke()
				}
				queuedActions.clear()
			}
		}

		fun call(function: () -> Unit, delay: Float, token: Any? = null)
		{
			synchronized(processing)
			{
				if (processing)
				{
					queuedActions.add {
						call(function, delay, token)
					}
				}
				else
				{
					if (token != null)
					{
						cancel(token)
					}

					pendingCalls.add(CallData(function, delay, token))
				}
			}
		}

		fun cancel(token: Any)
		{
			synchronized(processing)
			{
				if (processing)
				{
					queuedActions.add {
						cancel(token)
					}
				}
				else
				{
					val itr = pendingCalls.iterator()
					while (itr.hasNext())
					{
						val item = itr.next()

						if (item.token == token)
						{
							itr.remove()
						}
					}
				}
			}
		}
	}
}

data class CallData(val function: () -> Unit, var delay: Float, val token: Any?)
