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
	enum class RemainderMode
	{
		RULE,
		PAD,
		EXPAND
	}

	lateinit var remainderMode: RemainderMode
	var onX = true
	lateinit var size: String
	lateinit var rule: String
	lateinit var remainder: String
	var parallel = false

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		args.area.xMode = onX

		args.area.writeVariables(args.variables)

		val points = Array<RepeatDivision>()

		var current = 0
		val totalSize = args.area.size

		while (current < totalSize)
		{
			val size = size.evaluate(args.variables, rng.nextLong()).round()

			if (current + size > totalSize) break

			points.add(RepeatDivision(current, size))

			current += size
		}

		val remaining = totalSize - current
		if (remaining > 0)
		{
			if (remainderMode == RemainderMode.PAD)
			{
				val paddingRaw = remaining.toFloat() / points.size.toFloat()
				val padding = paddingRaw.toInt()
				val paddingRemainder = paddingRaw - padding

				var accumulatedOffset = 0
				var accumulatedRemainder = 0f
				for (point in points)
				{
					point.pos += accumulatedOffset
					accumulatedOffset += padding

					accumulatedRemainder += paddingRemainder
					if (accumulatedRemainder > 1f)
					{
						accumulatedRemainder -= 1f

						accumulatedOffset += 1
					}
				}
			}
			else if (remainderMode == RemainderMode.EXPAND)
			{
				val paddingRaw = remaining.toFloat() / points.size.toFloat()
				val padding = paddingRaw.toInt()
				val paddingRemainder = paddingRaw - padding

				var accumulatedOffset = 0
				var accumulatedRemainder = 0f
				for (point in points)
				{
					point.pos += accumulatedOffset
					point.size += padding
					accumulatedOffset += padding

					accumulatedRemainder += paddingRemainder
					if (accumulatedRemainder > 1f)
					{
						accumulatedRemainder -= 1f

						point.size += 1
						accumulatedOffset += 1
					}
				}
			}
		}

		val jobs = Array<Job>(8)

		for (point in points)
		{
			val newSeed = rng.nextLong()

			val newArea = args.area.copy()
			newArea.pos = args.area.pos + point.pos
			newArea.size = point.size

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

		if (remainderMode == RemainderMode.RULE && !remainder.isNullOrBlank() && remaining > 0)
		{
			val newSeed = rng.nextLong()

			val newArea = args.area.copy()
			newArea.pos = args.area.pos + current
			newArea.size = remaining

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
		remainderMode = RemainderMode.valueOf(xml.get("RemainderMode", "Rule").toUpperCase())
	}

}

data class RepeatDivision(var pos: Int, var size: Int)