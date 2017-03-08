package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate

class GrammarRuleRotate : AbstractGrammarRule()
{
	lateinit var degrees: String

	suspend override fun execute(args: RuleArguments)
	{
		args.area.orientation += degrees.evaluate(args.variables, args.seed)
	}

	override fun parse(xml: XmlReader.Element)
	{
		degrees = xml.get("Degrees")
	}
}
