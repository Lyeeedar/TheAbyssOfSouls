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

		fun update(delta: Float)
		{
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
		}

		fun call(function: () -> Unit, delay: Float, token: Any? = null)
		{
			if (token != null)
			{
				cancel(token)
			}

			pendingCalls.add(CallData(function, delay, token))
		}

		fun cancel(token: Any)
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

data class CallData(val function: () -> Unit, var delay: Float, val token: Any?)
