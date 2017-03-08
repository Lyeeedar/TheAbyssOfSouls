package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader

class GrammarRuleFlip : AbstractGrammarRule()
{
	var onX = true

	suspend override fun execute(args: RuleArguments)
	{
		if (onX)
		{
			args.area.flipX = !args.area.flipX
		}
		else
		{
			args.area.flipY = !args.area.flipY
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		onX = xml.get("Axis", "X") == "X"
	}

}
