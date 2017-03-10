package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Direction
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.freeTS
import com.lyeeedar.Util.removeRandom
import com.lyeeedar.Util.round

class GrammarRuleTake : AbstractGrammarRule()
{
	enum class Mode
	{
		RANDOM,
		CORNER,
		EDGE,
		CENTER
	}

	lateinit var mode: Mode
	lateinit var count: String
	var centerDist = 2
	var perPoint = false
	lateinit var rule: String
	lateinit var remainder: String

	suspend override fun execute(args: RuleArguments)
	{
		val valid = Array<Pos>(false, if (args.area.isPoints) args.area.points.size else args.area.width * args.area.height)
		valid.addAll(args.area.getAllPoints())

		if (mode == Mode.EDGE)
		{
			val existing = ObjectSet<Pos>()
			existing.addAll(valid)

			for (pos in existing)
			{
				var count = 0
				for (dir in Direction.CardinalValues)
				{
					val npos = Pos(pos.x + dir.x, pos.y + dir.y)
					if (!existing.contains(npos))
					{
						count++
					}
				}

				if (count == 0)
				{
					valid.removeValue(pos, true)
				}
			}
		}
		else if (mode == Mode.CORNER)
		{
			val existing = ObjectSet<Pos>()
			existing.addAll(valid)

			for (pos in existing)
			{
				var count = 0
				for (dir in Direction.CardinalValues)
				{
					val npos = Pos(pos.x + dir.x, pos.y + dir.y)
					if (!existing.contains(npos))
					{
						count++
					}
				}

				if (count < 2)
				{
					valid.removeValue(pos, true)
				}
			}
		}
		else if (mode == Mode.CENTER)
		{
			val existing = ObjectSet<Pos>()
			existing.addAll(valid)

			for (pos in existing)
			{
				var count = 0
				for (dir in Direction.CardinalValues)
				{
					for (i in 1..centerDist)
					{
						val npos = Pos(pos.x + dir.x * i, pos.y + dir.y * i)
						if (!existing.contains(npos))
						{
							count++
						}
					}
				}

				if (count != 0)
				{
					valid.removeValue(pos, true)
				}
			}
		}

		if (valid.size == 0) return

		args.area.writeVariables(args.variables)
		args.variables.put("count", valid.size.toFloat())

		val rng = Random.obtainTS(args.seed)

		val count = count.evaluate(args.variables, rng.nextLong()).round()

		var newArea: Area? = null
		if (count > 0)
		{
			val finalPoints = Array<Pos>()

			for (i in 0..count - 1)
			{
				finalPoints.add(valid.removeRandom(rng))
			}

			if (perPoint)
			{
				for (point in finalPoints)
				{
					newArea = args.area.copy()
					newArea.isPoints = true
					newArea.points.clear()
					newArea.points.add(point)

					val newArgs = args.copy(false, false, false, false)
					newArgs.area = newArea
					newArgs.seed = rng.nextLong()

					val rule = args.ruleTable[rule]
					rule.execute(newArgs)
				}
			}
			else
			{
				newArea = args.area.copy()
				if (!newArea.isPoints) newArea.convertToPoints()
				newArea.points.clear()
				newArea.points.addAll(finalPoints)

				val newArgs = args.copy(false, false, false, false)
				newArgs.area = newArea
				newArgs.seed = rng.nextLong()

				val rule = args.ruleTable[rule]
				rule.execute(newArgs)
			}
		}

		if (!remainder.isNullOrBlank() && valid.size > 0)
		{
			val remainderArea = newArea?.copy() ?: args.area.copy()
			if (!remainderArea.isPoints) remainderArea.convertToPoints()
			remainderArea.points.clear()
			remainderArea.points.addAll(valid)

			val newArgs = args.copy(false, false, false, false)
			newArgs.area = remainderArea
			newArgs.seed = rng.nextLong()

			val remainder = args.ruleTable[remainder]
			remainder.execute(newArgs)
		}

		rng.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "RANDOM").toUpperCase())
		count = xml.get("Count", "1").toLowerCase().replace("%", "#count").unescapeCharacters()
		centerDist = xml.getInt("Dist", 2)
		rule = xml.get("Rule")
		remainder = xml.get("Remainder", "")
		perPoint = xml.getBoolean("PerPoint", false)
	}

}
