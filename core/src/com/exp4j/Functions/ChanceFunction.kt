package com.exp4j.Functions

import com.badlogic.gdx.utils.Pool
import net.objecthunter.exp4j.function.Function
import squidpony.squidmath.LightRNG

/**
 * Created by Philip on 03-Jan-16.
 */
class ChanceFunction : Function("chance", 2)
{
	private val ran = LightRNG()

	fun set(seed: Long)
	{
		this.ran.setSeed(seed)
	}

	override fun apply(vararg arg0: Double): Double
	{
		val chanceVal = ran.nextFloat() * arg0[1]
		return (if (chanceVal <= arg0[0]) 1 else 0).toDouble()
	}

	fun free()
	{
		synchronized(pool)
		{
			pool.free(this)
		}
	}

	companion object
	{
		val pool = object : Pool<ChanceFunction>() {
			override fun newObject(): ChanceFunction
			{
				return ChanceFunction()
			}

		}

		fun obtain(): ChanceFunction
		{
			synchronized(pool)
			{
				return pool.obtain()
			}
		}
	}
}
