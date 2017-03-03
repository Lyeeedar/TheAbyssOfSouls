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
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS
import com.lyeeedar.Util.round
import kotlinx.coroutines.experimental.Job

class GrammarRuleDivide : AbstractGrammarRule()
{
	val divisions = Array<Division>()
	var onX = true
	var parallel = false

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		area.xMode = onX

		val rng = Random.obtainTS(seed)

		val jobs = Array<Job>(divisions.size)

		if (onX)
		{
			var current = 0
			for (i in 0..divisions.size - 1)
			{
				val newSeed = rng.nextLong()

				val division = divisions[i]

				area.writeVariables(variables)
				val size = if (division.size == "remainder") area.size - current else Math.min(area.size - current, division.size.evaluate(variables, rng.nextLong()).round())
				val newArea = area.copy()
				newArea.pos = area.pos + current
				newArea.size = size

				if (!division.rule.isNullOrBlank() && newArea.hasContents)
				{
					newArea.points.clear()
					newArea.addPointsWithin(area)

					val rule = ruleTable[division.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules))
					}
					else
					{
						rule.execute(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules)
					}
				}

				current += size
				if (current == area.size) break
			}
		}
		else
		{
			var current = area.size
			for (i in 0..divisions.size - 1)
			{
				val newSeed = rng.nextLong()

				val division = divisions[i]

				area.writeVariables(variables)
				val size = if (division.size == "remainder") current else Math.min(current, division.size.evaluate(variables, rng.nextLong()).round())
				current -= size

				val newArea = area.copy()
				newArea.pos = area.pos + current
				newArea.size = size

				if (!division.rule.isNullOrBlank() && newArea.hasContents)
				{
					newArea.points.clear()
					newArea.addPointsWithin(area)

					val rule = ruleTable[division.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules))
					}
					else
					{
						rule.execute(newArea, ruleTable, defines, variables, symbolTable, newSeed, deferredRules)
					}
				}

				if (current == 0) break
			}
		}

		for (job in jobs) job.join()

		rng.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		onX = xml.getAttribute("Axis", "X") == "X"
		parallel = xml.getBooleanAttribute("Parallel", false)

		for (el in xml.children())
		{
			val size = el.get("Size").toLowerCase().replace("%", "#size").unescapeCharacters()
			val rule = el.get("Rule", "")

			divisions.add(Division(size, rule))
		}
	}
}

data class Division(val size: String, val rule: String?)