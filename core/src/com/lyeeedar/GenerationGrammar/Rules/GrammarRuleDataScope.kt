package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import java.util.*

class GrammarRuleDataScope : AbstractGrammarRule()
{
	var scopeDefines = true
	var scopeVariables = true
	var scopeSymbols = true
	var scopeArea = true

	lateinit var child: String

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		var newDefines = defines
		var newVariables = variables
		var newSymbols = symbolTable
		var newArea = area

		if (scopeDefines)
		{
			newDefines = ObjectMap<String, String>()
			newDefines.putAll(defines)
		}

		if (scopeVariables)
		{
			newVariables = ObjectFloatMap()
			newVariables.putAll(variables)
		}

		if (scopeSymbols)
		{
			newSymbols = ObjectMap()
			symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }
		}

		if (scopeArea)
		{
			newArea = area.copy()
		}

		val rule = ruleTable[child]
		rule.execute(newArea, ruleTable, newDefines, newVariables, newSymbols, ran, deferredRules)
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