package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS

class GrammarRuleNode : AbstractGrammarRule()
{
	val rules = Array<AbstractGrammarRule>()

	suspend override fun execute(args: RuleArguments)
	{
		val rng = Random.obtainTS(args.seed)

		for (i in 0..rules.size-1)
		{
			val newArgs = args.copy(false, false, false, false)
			newArgs.seed = rng.nextLong()

			rules[i].execute(newArgs)
		}

		rng.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (el in xml.children())
		{
			rules.add(load(el))
		}
	}
}
