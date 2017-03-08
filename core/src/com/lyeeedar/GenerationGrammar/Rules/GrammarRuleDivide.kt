package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
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

	suspend override fun execute(args: RuleArguments)
	{
		args.area.xMode = onX

		val rng = Random.obtainTS(args.seed)

		val jobs = Array<Job>(divisions.size)

		if (onX)
		{
			var current = 0
			for (i in 0..divisions.size - 1)
			{
				val newSeed = rng.nextLong()

				val division = divisions[i]

				args.area.writeVariables(args.variables)
				val size = if (division.size == "remainder") args.area.size - current else Math.min(args.area.size - current, division.size.evaluate(args.variables, rng.nextLong()).round())
				val newArea = args.area.copy()
				newArea.pos = args.area.pos + current
				newArea.size = size

				if (!division.rule.isNullOrBlank() && newArea.hasContents)
				{
					newArea.points.clear()
					newArea.addPointsWithin(args.area)

					val newArgs = args.copy(false, false, false, false)
					newArgs.area = newArea
					newArgs.seed = newSeed

					val rule = args.ruleTable[division.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArgs))
					}
					else
					{
						rule.execute(newArgs)
					}
				}

				current += size
				if (current == args.area.size) break
			}
		}
		else
		{
			var current = args.area.size
			for (i in 0..divisions.size - 1)
			{
				val newSeed = rng.nextLong()

				val division = divisions[i]

				args.area.writeVariables(args.variables)
				val size = if (division.size == "remainder") current else Math.min(current, division.size.evaluate(args.variables, rng.nextLong()).round())
				current -= size

				val newArea = args.area.copy()
				newArea.pos = args.area.pos + current
				newArea.size = size

				if (!division.rule.isNullOrBlank() && newArea.hasContents)
				{
					newArea.points.clear()
					newArea.addPointsWithin(args.area)

					val newArgs = args.copy(false, false, false, false)
					newArgs.area = newArea
					newArgs.seed = newSeed

					val rule = args.ruleTable[division.rule]

					if (parallel)
					{
						jobs.add(rule.executeAsync(newArgs))
					}
					else
					{
						rule.execute(newArgs)
					}
				}

				if (current == 0) break
			}
		}

		rng.freeTS()

		for (job in jobs) job.join()
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