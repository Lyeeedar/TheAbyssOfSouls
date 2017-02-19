package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import java.util.*

class GrammarRuleScale : AbstractGrammarRule()
{
	var additive = false
	var x: Float = 1f
	var y: Float = 1f

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val oldWidth = area.width
		val oldHeight = area.height

		if (additive)
		{
			area.width += x.toInt()
			area.height += y.toInt()
		}
		else
		{
			area.width = (area.width * x).toInt()
			area.height = (area.height * y).toInt()
		}

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
		additive = xml.get("Mode", "Additive") == "Additive"
		x = xml.getFloat("X")
		y = xml.getFloat("Y")
	}

}
