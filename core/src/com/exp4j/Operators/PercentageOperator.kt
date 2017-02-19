package com.exp4j.Operators

import net.objecthunter.exp4j.operator.Operator

class PercentageOperator : Operator("#", 2, true, Operator.PRECEDENCE_UNARY_PLUS + 1)
{
	override fun apply(vararg args: Double): Double
	{
		return args[0] / 100.0 * args[1]
	}

	companion object
	{
		val operator = PercentageOperator()
	}
}