package com.exp4j.Helpers

import java.util.HashMap
import java.util.Random

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectIntMap
import com.exp4j.Functions.ChanceFunction
import com.exp4j.Functions.MathUtilFunctions
import com.exp4j.Functions.RandomFunction
import com.exp4j.Operators.BooleanOperators
import com.lyeeedar.Statistic
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder

class EquationHelper
{
	companion object
	{
		private val emptyMap = Statistic.emptyMap

		fun tryBuild(expB: ExpressionBuilder): Expression?
		{
			var exp: Expression?

			// try
			// {
			exp = expB.build()
			// }
			// catch (Exception e) { }

			return exp
		}

		fun setVariableNames(expB: ExpressionBuilder, variableMap: ObjectFloatMap<String>, prefix: String)
		{
			for (key in variableMap.keys())
			{
				expB.variable(prefix + key)
			}
		}

		fun setVariableValues(exp: Expression, variableMap: ObjectFloatMap<String>, prefix: String)
		{
			for (key in variableMap.keys())
			{
				exp.setVariable(prefix + key, variableMap.get(key, 0f).toDouble())
			}
		}

		@JvmOverloads fun createEquationBuilder(eqn: String, ran: Random = MathUtils.random): ExpressionBuilder
		{
			val expB = ExpressionBuilder(eqn)
			BooleanOperators.applyOperators(expB)
			expB.function(RandomFunction(ran))
			expB.function(ChanceFunction(ran))
			MathUtilFunctions.applyFunctions(expB)

			return expB
		}

		fun isNumber(string: String?): Boolean
		{
			if (string == null || string.isEmpty())
			{
				return false
			}
			var i = 0
			if (string[0] == '-')
			{
				if (string.length > 1)
				{
					i++
				} else
				{
					return false
				}
			}
			while (i < string.length)
			{
				if (!Character.isDigit(string[i]))
				{
					return false
				}
				i++
			}
			return true
		}

		fun evaluate(eqn: String, variableMap: ObjectFloatMap<String> = emptyMap, ran: Random = MathUtils.random): Float
		{
			if (isNumber(eqn))
			{
				return Integer.parseInt(eqn).toFloat()
			} else
			{
				val expB = createEquationBuilder(eqn, ran)
				setVariableNames(expB, variableMap, "")
				val exp = tryBuild(expB)

				if (exp == null)
				{
					return 0f
				} else
				{
					setVariableValues(exp, variableMap, "")
					val rawVal = exp.evaluate()

					return rawVal.toFloat()
				}
			}
		}
	}
}
