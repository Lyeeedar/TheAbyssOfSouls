package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader

class GrammarRuleRule : AbstractGrammarRule()
{
	lateinit var child: String

	suspend override fun execute(args: RuleArguments)
	{
		val rule = args.ruleTable[child]
		rule.execute(args)
	}

	override fun parse(xml: XmlReader.Element)
	{
		child = xml.get("Rule")
	}
}
