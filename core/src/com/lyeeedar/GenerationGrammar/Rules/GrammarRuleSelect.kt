package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Direction
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.random
import com.lyeeedar.Util.round
import ktx.collections.toGdxArray
import java.util.*

class GrammarRuleSelect : AbstractGrammarRule()
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
	lateinit var rule: String

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random)
	{
		val valid = Array<Pos>()
		valid.addAll(area.getAllPoints())

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

		variables.put("count", valid.size.toFloat())

		val count = count.evaluate(variables, ran).round()

		variables.remove("count", 0f)

		if (count == 0) return

		val finalPoints = valid.asSequence().random(count, ran).asIterable().toGdxArray()

		val newArea = area.copy()
		if (!newArea.isPoints) newArea.convertToPoints()
		newArea.points.clear()
		newArea.points.addAll(finalPoints)

		val rule = ruleTable[rule]
		rule.execute(newArea, ruleTable, defines, variables, symbolTable, ran)
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "RANDOM").toUpperCase())
		count = xml.get("Count").toLowerCase().replace("%", "#count")
		centerDist = xml.getInt("Dist")
		rule = xml.get("Rule")
	}

}
