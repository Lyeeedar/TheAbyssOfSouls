package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.freeTS
import com.lyeeedar.Util.round
import kotlinx.coroutines.experimental.Job

class GrammarRuleRepeat : AbstractGrammarRule()
{
	var onX = true
	lateinit var size: String
	lateinit var rule: String
	lateinit var remainder: String
	var parallel = false

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		val rng = Random.obtainTS(seed)

		area.xMode = onX

		var current = 0
		val totalSize = area.size

		val jobs = Array<Job>(8)

		while (current < totalSize)
		{
			val newSeed = rng.nextLong()

			area.writeVariables(variables)
			val size = size.evaluate(variables, rng.nextLong()).round()

			val newArea = area.copy()
			newArea.pos = area.pos + current
			newArea.size = Math.min(size, totalSize-current)

			if (current + size <= totalSize)
			{
				newArea.points.clear()
				newArea.addPointsWithin(area)

				if (newArea.hasContents)
				{
					val rule = ruleTable[rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules))
					}
					else
					{
						rule.execute(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules)
					}
				}
			}
			else
			{
				if (!remainder.isNullOrBlank())
				{
					newArea.points.clear()
					newArea.addPointsWithin(area)

					if (newArea.hasContents)
					{
						val rule = ruleTable[remainder]

						if (parallel)
						{
							jobs.add(rule.executeAsync(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules))
						}
						else
						{
							rule.execute(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules)
						}
					}
				}

				break
			}

			current += size
		}

		for (job in jobs) job.join()

		rng.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		onX = xml.get("Axis", "X") == "X"
		parallel = xml.getBoolean("Parallel", false)
		size = xml.get("Size").replace("%", "#size").unescapeCharacters()
		rule = xml.get("Rule")
		remainder = xml.get("Remainder", "")
	}

}
