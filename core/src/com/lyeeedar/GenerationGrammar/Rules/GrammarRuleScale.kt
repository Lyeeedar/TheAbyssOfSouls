package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import java.util.*

class GrammarRuleScale : AbstractGrammarRule()
{
	enum class Mode
	{
		ADDITIVE,
		MULTIPLICATIVE,
		ABSOLUTE
	}

	lateinit var mode: Mode
	lateinit var xEqn: String
	lateinit var yEqn: String

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val oldWidth = area.width
		val oldHeight = area.height

		area.writeVariables(variables)
		val x = xEqn.evaluate(variables, ran)
		val y = yEqn.evaluate(variables, ran)

		if (mode == Mode.ADDITIVE)
		{
			area.width += x.toInt()
			area.height += y.toInt()
		}
		else if (mode == Mode.MULTIPLICATIVE)
		{
			area.width = (area.width * x).toInt()
			area.height = (area.height * y).toInt()
		}
		else if (mode == Mode.ABSOLUTE)
		{
			area.width = x.toInt()
			area.height = y.toInt()
		}
		else throw Exception("Unhandled scale mode '$mode'!")

		val diffX = area.width - oldWidth
		val diffY = area.height - oldHeight

		area.x -= diffX / 2
		area.y -= diffY / 2

		if (area.isPoints)
		{
			for (point in area.points.toList())
			{
				if (point.x < area.x || point.y < area.y || point.x >= area.x+area.width || point.y >= area.y+area.height)
				{
					area.points.removeValue(point, true)
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		mode = Mode.valueOf(xml.get("Mode", "Additive").toUpperCase())
		xEqn = xml.get("X")
		yEqn = xml.get("Y")
	}

}
