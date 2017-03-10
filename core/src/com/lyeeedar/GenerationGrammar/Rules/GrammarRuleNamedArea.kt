package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Util.*
import kotlinx.coroutines.experimental.Job
import ktx.collections.toGdxArray

enum class Mode
{
	LARGEST,
	SMALLEST,
	RANDOM
}

class GrammarRuleNamedArea : AbstractGrammarRule()
{
	val datas = Array<Data>()

	lateinit var key: String

	var parallel = false

	suspend override fun execute(args: RuleArguments)
	{
		val rooms = args.namedAreas[key]?.toGdxArray() ?: return

		if (rooms.size == 0) return

		val jobs = Array<Job>(rooms.size)
		val rng = Random.obtainTS(args.seed)

		for (data in datas)
		{
			val selectionList = when(data.mode)
			{
				Mode.RANDOM -> rooms.asGdxArray()
				Mode.SMALLEST -> rooms.sortedBy { it.width * it.height }.asGdxArray()
				Mode.LARGEST -> rooms.sortedByDescending { it.width * it.height }.asGdxArray()
				else -> throw Exception("Unhandled named area mode '${data.mode}'!")
			}

			args.area.writeVariables(args.variables)
			args.variables.put("count", rooms.size.toFloat())

			var count = if (data.count == "remainder") selectionList.size else data.count.evaluate(args.variables, rng.nextLong()).round()
			count = Math.min(selectionList.size, count)

			if (count > 0)
			{
				for (i in 0..count - 1)
				{
					val area = when (data.mode)
					{
						Mode.RANDOM -> selectionList.removeRandom(rng)
						Mode.LARGEST, Mode.SMALLEST -> rooms[i]
						else -> throw Exception("Unhandled named area mode '${data.mode}'!")
					}

					rooms.removeValue(area, true)

					if (!data.rule.isNullOrBlank())
					{
						val newArgs = args.copy()
						newArgs.area = area.copy()
						newArgs.seed = rng.nextLong()

						val rule = args.ruleTable[data.rule]

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
			}

			if (rooms.size == 0) break
		}

		rng.freeTS()

		for (job in jobs) job.join()
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
		parallel = xml.getBoolean("Parallel", false)

		val rulesEl = xml.getChildByName("Rules")
		for (el in rulesEl.children())
		{
			val mode = Mode.valueOf(el.get("Mode", "RANDOM").toUpperCase())
			val count = el.get("Count", "1").toLowerCase().replace("%", "#count").unescapeCharacters()
			val rule = el.get("Rule")

			datas.add(Data(mode, count, rule))
		}
	}
}

data class Data(val mode: Mode, val count: String, val rule: String)
