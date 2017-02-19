package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.GenerationGrammar.Pos
import java.util.*

class GrammarRuleTranslate : AbstractGrammarRule()
{
	var x: Int = 0
	var y: Int = 0

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		area.x += x
		area.y += y

		if (area.isPoints)
		{
			area.points.forEachIndexed { i, pos -> area.points[i] = Pos(pos.x + x, pos.y + y) }
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		x = xml.getInt("X", 0)
		y = xml.getInt("Y", 0)
	}

}
