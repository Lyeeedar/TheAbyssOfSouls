package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Util.*
import kotlinx.coroutines.experimental.Job
import ktx.collections.toGdxArray

class GrammarRuleNamedArea : AbstractGrammarRule()
{
	enum class Mode
	{
		LARGEST,
		SMALLEST,
		RANDOM
	}

	lateinit var key: String
	lateinit var mode: Mode
	lateinit var count: String
	var parallel = false

	lateinit var rule: String
	lateinit var remainder: String

	suspend override fun execute(args: RuleArguments)
	{
		var rooms = args.namedAreas[key] ?: return

		if (rooms.size == 0) return

		val jobs = Array<Job>(rooms.size)

		rooms = when(mode)
		{
			Mode.RANDOM -> Array(rooms)
			Mode.SMALLEST -> rooms.sortedBy { it.width * it.height }.asGdxArray()
			Mode.LARGEST -> rooms.sortedByDescending { it.width * it.height }.asGdxArray()
			else -> throw Exception("Unhandled named area mode '$mode'!")
		}

		args.area.writeVariables(args.variables)
		args.variables.put("count", rooms.size.toFloat())

		val rng = Random.obtainTS(args.seed)

		val count = count.evaluate(args.variables, rng.nextLong()).round()

		for (i in 0..count-1)
		{
			val area = when (mode)
			{
				Mode.RANDOM -> rooms.removeRandom(rng)
				Mode.LARGEST, Mode.SMALLEST -> rooms[i]
				else -> throw Exception("Unhandled named area mode '$mode'!")
			}

			val newArgs = args.copy()
			newArgs.area = area.copy()
			newArgs.seed = rng.nextLong()

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

		if (!remainder.isNullOrBlank())
		{
			val remaining = when (mode)
			{
				Mode.RANDOM -> rooms
				Mode.LARGEST, Mode.SMALLEST -> rooms.toList().slice(count..rooms.size-1).toGdxArray()
				else -> throw Exception("Unhandled named area mode '$mode'!")
			}

			for (room in remaining)
			{
				val newArgs = args.copy()
				newArgs.area = room.copy()
				newArgs.seed = rng.nextLong()

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
		key = xml.get("Key")
		parallel = xml.getBoolean("Parallel", false)
		mode = Mode.valueOf(xml.get("Mode", "RANDOM").toUpperCase())
		count = xml.get("Count", "1").toLowerCase().replace("%", "#count").unescapeCharacters()
		rule = xml.get("Rule")
		remainder = xml.get("Remainder", "")
	}
}
