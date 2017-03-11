package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global

class GrammarRuleBreakpoint : AbstractGrammarRule()
{
	lateinit var name: String

	suspend override fun execute(args: RuleArguments)
	{
		if (!Global.release) println("Breakpoint '$name' hit")
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name")
	}
}
