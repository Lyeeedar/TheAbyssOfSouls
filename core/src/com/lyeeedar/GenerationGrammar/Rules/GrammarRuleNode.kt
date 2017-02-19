package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.children
import java.util.*

class GrammarRuleNode : AbstractGrammarRule()
{
	val rules = Array<AbstractGrammarRule>()

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		for (i in 0..rules.size-1)
		{
			rules[i].execute(area, ruleTable, defines, variables, symbolTable, ran, deferredRules)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (el in xml.children())
		{
			rules.add(load(el))
		}
	}
}
