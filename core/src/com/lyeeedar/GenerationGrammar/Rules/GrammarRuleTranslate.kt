package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.freeTS

class GrammarRuleTranslate : AbstractGrammarRule()
{
	lateinit var xEqn: String
	lateinit var yEqn: String

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		val rng = Random.obtainTS(seed)

		area.writeVariables(variables)
		val x = xEqn.evaluate(variables, rng.nextLong()).toInt()
		val y = yEqn.evaluate(variables, rng.nextLong()).toInt()

		rng.freeTS()

		area.x += x
		area.y += y

		if (area.isPoints)
		{
			area.points.forEachIndexed { i, pos -> area.points[i] = Pos(pos.x + x, pos.y + y) }
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		xEqn = xml.get("X", "0")
		yEqn = xml.get("Y", "0")
	}

}
