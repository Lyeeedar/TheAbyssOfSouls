package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.SpaceSlot
import java.util.*

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
	var char: Char = ' '

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val condition: (symbol: GrammarSymbol) -> Boolean = when (mode)
		{
			Mode.NOTWALL -> fun (symbol) = !symbol.contents.containsKey(SpaceSlot.WALL)
			Mode.NOTENTITY -> fun (symbol) = !symbol.contents.containsKey(SpaceSlot.ENTITY)
			Mode.CHARACTER ->  fun (symbol) = symbol.char == char
			else -> throw UnsupportedOperationException("Unknown mode '$mode'!")
		}

		val newArea = area.copy()

		if (!newArea.isPoints) newArea.convertToPoints()

		for (point in newArea.points.toList())
		{
			val symbol = newArea[point.x - newArea.x, point.y - newArea.y]

			if (symbol == null || !condition.invoke(symbol)) newArea.points.removeValue(point, true)
		}

		if (newArea.points.size > 0)
		{
			val rule = ruleTable[rule]
			rule.execute(newArea, ruleTable, defines, variables, symbolTable, ran, deferredRules)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "NotWall").toUpperCase())
		rule = xml.get("Rule")
		char = xml.get("Character", " ")[0]
	}

}
