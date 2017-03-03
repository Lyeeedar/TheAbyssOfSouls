package com.exp4j.Functions

import com.badlogic.gdx.utils.Pool
import net.objecthunter.exp4j.function.Function
import squidpony.squidmath.LightRNG

class RandomFunction : Function("rnd", 1)
{
	private val ran = LightRNG()

	fun set(seed: Long)
	{
		this.ran.setSeed(seed)
	}

	override fun apply(vararg arg0: Double): Double
	{
		return ran.nextFloat() * arg0[0]
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

		val pool = object : Pool<RandomFunction>() {
			override fun newObject(): RandomFunction
			{
				return RandomFunction()
			}
		}

		fun obtain(): RandomFunction
		{
			synchronized(pool)
			{
				return pool.obtain()
			}
		}
	}
}
