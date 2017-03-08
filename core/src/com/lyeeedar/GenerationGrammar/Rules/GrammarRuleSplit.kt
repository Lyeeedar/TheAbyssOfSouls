package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS
import com.lyeeedar.Util.round
import kotlinx.coroutines.experimental.Job

class GrammarRuleSplit : AbstractGrammarRule()
{
	enum class SplitSide
	{
		NORTH,
		SOUTH,
		EAST,
		WEST,
		REMAINDER
	}

	val splits = Array<Split>()
	var parallel = false

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		val jobs = Array<Job>(splits.size)

		var currentArea = args.area.copy()
		for (i in 0..splits.size-1)
		{
			val newSeed = rng.nextLong()

			val split = splits[i]

			val newArea = currentArea.copy()
			val nextArea = currentArea.copy()

			if (split.side == SplitSide.NORTH)
			{
				currentArea.xMode = false
				currentArea.writeVariables(args.variables)
				val size = split.size.evaluate(args.variables, rng.nextLong()).round()

				newArea.x = currentArea.x
				newArea.y = currentArea.y + currentArea.height - size
				newArea.width = currentArea.width
				newArea.height = size
				newArea.points.clear()
				newArea.addPointsWithin(currentArea)

				nextArea.x = currentArea.x
				nextArea.y = currentArea.y
				nextArea.width = currentArea.width
				nextArea.height = currentArea.height - size
				nextArea.points.clear()
				nextArea.addPointsWithin(currentArea)
			}
			else if (split.side == SplitSide.SOUTH)
			{
				currentArea.xMode = false
				currentArea.writeVariables(args.variables)
				val size = split.size.evaluate(args.variables, rng.nextLong()).round()

				newArea.x = currentArea.x
				newArea.y = currentArea.y
				newArea.width = currentArea.width
				newArea.height = size
				newArea.points.clear()
				newArea.addPointsWithin(currentArea)

				nextArea.x = currentArea.x
				nextArea.y = currentArea.y + size
				nextArea.width = currentArea.width
				nextArea.height = currentArea.height - size
				nextArea.points.clear()
				nextArea.addPointsWithin(currentArea)
			}
			else if (split.side == SplitSide.WEST)
			{
				currentArea.xMode = true
				currentArea.writeVariables(args.variables)
				val size = split.size.evaluate(args.variables, rng.nextLong()).round()

				newArea.x = currentArea.x
				newArea.y = currentArea.y
				newArea.width = size
				newArea.height = currentArea.height
				newArea.points.clear()
				newArea.addPointsWithin(currentArea)

				nextArea.x = currentArea.x + size
				nextArea.y = currentArea.y
				nextArea.width = currentArea.width - size
				nextArea.height = currentArea.height
				nextArea.points.clear()
				nextArea.addPointsWithin(currentArea)
			}
			else if (split.side == SplitSide.EAST)
			{
				currentArea.xMode = true
				currentArea.writeVariables(args.variables)
				val size = split.size.evaluate(args.variables, rng.nextLong()).round()

				newArea.x = currentArea.x + currentArea.width - size
				newArea.y = currentArea.y
				newArea.width = size
				newArea.height = currentArea.height
				newArea.points.clear()
				newArea.addPointsWithin(currentArea)

				nextArea.x = currentArea.x
				nextArea.y = currentArea.y
				nextArea.width = currentArea.width - size
				nextArea.height = currentArea.height
				nextArea.points.clear()
				nextArea.addPointsWithin(currentArea)
			}
			else if (split.side == SplitSide.REMAINDER)
			{
				if (!split.rule.isNullOrBlank())
				{
					val newArgs = args.copy(false, false, false, false)
					newArgs.area = currentArea
					newArgs.seed = newSeed

					val rule = args.ruleTable[split.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArgs))
					}
					else
					{
						rule.execute(newArgs)
					}
				}

				break
			}
			else throw Exception("Unhandled split side '" + split.side + "'!")

			currentArea = nextArea

			if (!split.rule.isNullOrBlank())
			{
				val newArgs = args.copy(false, false, false, false)
				newArgs.area = newArea
				newArgs.seed = newSeed

				val rule = args.ruleTable[split.rule]

				if (parallel)
				{
					jobs.add(rule.executeAsync(newArgs))
				}
				else
				{
					rule.execute(newArgs)
				}
			}
		}

		rng.freeTS()

		for (job in jobs) job.join()
	}

	override fun parse(xml: XmlReader.Element)
	{
		parallel = xml.getBooleanAttribute("Parallel", false)

		for (el in xml.children())
		{
			val side = SplitSide.valueOf(el.get("Side", "North").toUpperCase())
			val size = el.get("Size", "1")
			val rule = el.get("Rule")

			splits.add(Split(side, size, rule))
		}
	}
}

data class Split(val side: GrammarRuleSplit.SplitSide, val size: String, val rule: String)