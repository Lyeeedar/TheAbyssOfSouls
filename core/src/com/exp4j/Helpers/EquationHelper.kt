package com.exp4j.Helpers

import com.badlogic.gdx.utils.ObjectFloatMap
import com.exp4j.Functions.ChanceFunction
import com.exp4j.Functions.MathUtilFunctions
import com.exp4j.Functions.RandomFunction
import com.exp4j.Operators.BooleanOperators
import com.exp4j.Operators.PercentageOperator
import com.lyeeedar.Util.Random
import net.objecthunter.exp4j.Expression
import net.objecthunter.exp4j.ExpressionBuilder

class EquationHelper
{
	companion object
	{
		fun setVariableNames(expB: ExpressionBuilder, variableMap: ObjectFloatMap<String>)
		{
			for (key in variableMap.keys())
			{
				expB.variable(key)
			}
		}

		fun setVariableValues(exp: Expression, variableMap: ObjectFloatMap<String>)
		{
			val valuesToBeSet = exp.variableNames

			for (key in variableMap.keys())
			{
				exp.setVariable(key, variableMap.get(key, 0f).toDouble())

				valuesToBeSet.remove(key)
			}

			for (variable in valuesToBeSet)
			{
				exp.setVariable(variable, 0.0)
			}
		}

		fun evaluate(eqn: String, variableMap: ObjectFloatMap<String> = ObjectFloatMap(), seed: Long? = null): Float
		{
			try
			{
				return eqn.toFloat()
			}
			catch (ex: Exception)
			{
				val expB = ExpressionBuilder(eqn)
				expB.exceptionOnMissingVariables = false

				BooleanOperators.applyOperators(expB)
				expB.operator(PercentageOperator.operator)

				val seed = seed ?: Random.random.nextLong()
				val randomFun = RandomFunction.obtain()
				val chanceFun = ChanceFunction.obtain()
				randomFun.set(seed)
				chanceFun.set(seed)

				expB.function(randomFun)
				expB.function(chanceFun)
				MathUtilFunctions.applyFunctions(expB)

				setVariableNames(expB, variableMap)
				val exp = expB.build()

				val expectedVariables = exp.variableNames
				for (name in expectedVariables)
				{
					if (!variableMap.containsKey(name))
					{
						exp.setVariable(name, 0.0)
					}
				}

				if (exp == null)
				{
					randomFun.free()
					chanceFun.free()
					return 0f
				}
				else
				{
					setVariableValues(exp, variableMap)
					val rawVal = exp.evaluate()

					randomFun.free()
					chanceFun.free()

					return rawVal.toFloat()
				}
			}
		}
	}
}

fun String.unescapeCharacters(): String
{
	var output = this
	output = output.replace("&gt;", ">")
	output = output.replace("&lt;", "<")
	output = output.replace("&amp;", "&")
	return output
}

fun String.evaluate(variableMap: ObjectFloatMap<String> = ObjectFloatMap(), seed: Long? = null): Float = EquationHelper.evaluate(this, variableMap, seed)