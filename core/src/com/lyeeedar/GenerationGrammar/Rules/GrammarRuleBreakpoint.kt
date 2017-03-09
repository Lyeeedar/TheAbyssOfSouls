package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader

class GrammarRuleBreakpoint : AbstractGrammarRule()
{
	lateinit var name: String

	suspend override fun execute(args: RuleArguments)
	{
		println("Breakpoint '$name' hit")
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name")
	}
}
