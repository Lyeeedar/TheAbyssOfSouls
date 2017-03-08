package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS

class GrammarRuleCondition : AbstractGrammarRule()
{
	val conditions = Array<Condition>()

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		for (i in 0..conditions.size-1)
		{
			val newSeed = rng.nextLong()

			val condition = conditions[i]

			args.area.writeVariables(args.variables)
			if (condition.condition == "else" || condition.condition.evaluate(args.variables, rng.nextLong()) > 0)
			{
				if (!condition.rule.isNullOrBlank())
				{
					val rule = args.ruleTable[condition.rule]
					rule.execute(args)
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