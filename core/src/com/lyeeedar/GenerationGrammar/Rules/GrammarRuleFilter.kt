package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.SpaceSlot

class GrammarRuleFilter : AbstractGrammarRule()
{
	enum class Mode
	{
		NOTWALL,
		NOTENTITY,
		CHARACTER
	}

	lateinit var mode: Mode
	lateinit var rule: String
	lateinit var remainder: String
	var char: Char = ' '

	suspend override fun execute(args: RuleArguments)
	{
		val condition: (symbol: GrammarSymbol) -> Boolean = when (mode)
		{
			Mode.NOTWALL -> fun (symbol) = !symbol.contents.containsKey(SpaceSlot.WALL)
			Mode.NOTENTITY -> fun (symbol) = SpaceSlot.EntityValues.all { !symbol.contents.containsKey(it) }
			Mode.CHARACTER ->  fun (symbol) = symbol.char == char
			else -> throw UnsupportedOperationException("Unknown mode '$mode'!")
		}

		val newArea = args.area.copy()

		if (!newArea.isPoints) newArea.convertToPoints()

		val remainderArea = if (remainder.isNotBlank()) newArea.copy() else null
		remainderArea?.points?.clear()

		for (point in newArea.points.toList())
		{
			val symbol = newArea[point.x - newArea.x, point.y - newArea.y]

			if (symbol == null || !condition.invoke(symbol))
			{
				newArea.points.removeValue(point, true)
				remainderArea?.points?.add(point)
			}
		}

		if (rule.isNotBlank() && newArea.points.size > 0)
		{
			val newArgs = args.copy(false, false, false, false)
			newArgs.area = newArea

			val rule = args.ruleTable[rule]
			rule.execute(newArgs)
		}
		if (remainderArea != null && remainderArea.points.size > 0)
		{
			val newArgs = args.copy(false, false, false, false)
			newArgs.area = remainderArea

			val rule = args.ruleTable[remainder]
			rule.execute(newArgs)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "NotWall").toUpperCase())
		rule = xml.get("Rule", "")
		char = xml.get("Character", " ")[0]
		remainder = xml.get("Remainder", "")
	}

}
