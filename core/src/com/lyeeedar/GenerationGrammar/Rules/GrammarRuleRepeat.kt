package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
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

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		args.area.xMode = onX

		var current = 0
		val totalSize = args.area.size

		val jobs = Array<Job>(8)

		while (current < totalSize)
		{
			val newSeed = rng.nextLong()

			args.area.writeVariables(args.variables)
			val size = size.evaluate(args.variables, rng.nextLong()).round()

			val newArea = args.area.copy()
			newArea.pos = args.area.pos + current
			newArea.size = Math.min(size, totalSize-current)

			if (current + size <= totalSize)
			{
				newArea.points.clear()
				newArea.addPointsWithin(args.area)

				if (newArea.hasContents)
				{
					val newArgs = args.copy(false, false, false, false)
					newArgs.area = newArea
					newArgs.seed = newSeed

					val rule = args.ruleTable[rule]

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
			else
			{
				if (!remainder.isNullOrBlank())
				{
					newArea.points.clear()
					newArea.addPointsWithin(args.area)

					if (newArea.hasContents)
					{
						val newArgs = args.copy(false, false, false, false)
						newArgs.area = newArea
						newArgs.seed = newSeed

						val rule = args.ruleTable[remainder]

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

				break
			}

			current += size
		}

		rng.freeTS()

		for (job in jobs) job.join()
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
