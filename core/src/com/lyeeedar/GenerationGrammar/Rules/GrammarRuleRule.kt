package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import java.util.*

class GrammarRuleRule : AbstractGrammarRule()
{
	lateinit var child: String

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val rule = ruleTable[child]
		rule.execute(area, ruleTable, defines, variables, symbolTable, ran, deferredRules)
	}

	override fun parse(xml: XmlReader.Element)
	{
		child = xml.get("Rule")
	}
}
