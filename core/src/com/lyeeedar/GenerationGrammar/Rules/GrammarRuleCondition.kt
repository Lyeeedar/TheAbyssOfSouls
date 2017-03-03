package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS

class GrammarRuleCondition : AbstractGrammarRule()
{
	val conditions = Array<Condition>()

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		val rng = Random.obtainTS(seed)

		for (i in 0..conditions.size-1)
		{
			val newSeed = rng.nextLong()

			val condition = conditions[i]

			area.writeVariables(variables)
			if (condition.condition == "else" || condition.condition.evaluate(variables, rng.nextLong()) > 0)
			{
				if (!condition.rule.isNullOrBlank())
				{
					val rule = ruleTable[condition.rule]
					rule.execute(area, ruleTable, defines, variables, symbolTable, newSeed, deferredRules)
				}

				break
			}
		}

		rng.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (el in xml.children())
		{
			val condition = el.get("Condition").toLowerCase().replace("%", "#size").unescapeCharacters()
			val rule = el.get("Rule", "")

			conditions.add(Condition(condition, rule))
		}
	}

}

data class Condition(val condition: String, val rule: String)