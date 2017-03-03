package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.children
import com.lyeeedar.Util.round
import kotlinx.coroutines.experimental.Job
import java.util.*

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

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val jobs = Array<Job>(splits.size)

		var currentArea = area.copy()
		for (i in 0..splits.size-1)
		{
			val split = splits[i]

			val newArea = currentArea.copy()
			val nextArea = currentArea.copy()

			if (split.side == SplitSide.NORTH)
			{
				currentArea.xMode = false
				currentArea.writeVariables(variables)
				val size = split.size.evaluate(variables, ran).round()

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
				currentArea.writeVariables(variables)
				val size = split.size.evaluate(variables, ran).round()

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
			else if (split.side == SplitSide.EAST)
			{
				currentArea.xMode = true
				currentArea.writeVariables(variables)
				val size = split.size.evaluate(variables, ran).round()

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
			else if (split.side == SplitSide.WEST)
			{
				currentArea.xMode = true
				currentArea.writeVariables(variables)
				val size = split.size.evaluate(variables, ran).round()

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
					val rule = ruleTable[split.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(currentArea, ruleTable, defines, variables, symbolTable, ran, deferredRules))
					}
					else
					{
						rule.execute(currentArea, ruleTable, defines, variables, symbolTable, ran, deferredRules)
					}
				}

				break
			}
			else throw Exception("Unhandled split side '" + split.side + "'!")

			currentArea = nextArea

			if (!split.rule.isNullOrBlank())
			{
				val rule = ruleTable[split.rule]

				if (parallel)
				{
					jobs.add(rule.executeAsync(newArea, ruleTable, defines, variables, symbolTable, ran, deferredRules))
				}
				else
				{
					rule.execute(newArea, ruleTable, defines, variables, symbolTable, ran, deferredRules)
				}
			}
		}

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