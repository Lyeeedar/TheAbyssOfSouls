package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol

class GrammarRuleDataScope : AbstractGrammarRule()
{
	var scopeDefines = true
	var scopeVariables = true
	var scopeSymbols = true
	var scopeArea = true

	lateinit var child: String

	suspend override fun execute(args: RuleArguments)
	{
		val cpy = args.copy(scopeArea, scopeDefines, scopeVariables, scopeSymbols)

		val rule = cpy.ruleTable[child]
		rule.execute(cpy)
	}

	override fun parse(xml: XmlReader.Element)
	{
		scopeDefines = xml.getBoolean("Defines", true)
		scopeVariables = xml.getBoolean("Variables", true)
		scopeSymbols = xml.getBoolean("Symbols", true)
		scopeArea = xml.getBoolean("Area", true)
		child = xml.get("Rule")
	}
}