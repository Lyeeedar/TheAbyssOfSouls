package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol

class GrammarRuleRule : AbstractGrammarRule()
{
	lateinit var child: String

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		val rule = ruleTable[child]
		rule.execute(area, ruleTable, defines, variables, symbolTable, seed, deferredRules)
	}

	override fun parse(xml: XmlReader.Element)
	{
		child = xml.get("Rule")
	}
}
