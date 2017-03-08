package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import ktx.collections.set

class GrammarRuleDefer : AbstractGrammarRule()
{
	lateinit var rule: String

	suspend override fun execute(args: RuleArguments)
	{
		val rule = args.ruleTable[rule]

		synchronized(args.deferredRules)
		{
			args.deferredRules.add(DeferredRule(rule, args))
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		rule = xml.get("Rule")
	}
}

data class DeferredRule(val rule: AbstractGrammarRule, val args: RuleArguments)
{
	init
	{
		if (!areaMap.containsKey(args.area))
		{
			areaMap[args.area] = args.area.copy()
		}
		if (!defineMap.containsKey(args.defines))
		{
			val newDefines = ObjectMap<String, String>()
			newDefines.putAll(args.defines)

			defineMap[args.defines] = newDefines
		}
		if (!variableMap.containsKey(args.variables))
		{
			val newVariables = ObjectFloatMap<String>()
			newVariables.putAll(args.variables)

			variableMap[args.variables] = newVariables
		}
		if (!symbolMap.containsKey(args.symbolTable))
		{
			val newSymbols = ObjectMap<Char, GrammarSymbol>()
			args.symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }

			symbolMap[args.symbolTable] = newSymbols
		}
	}

	suspend fun execute(ruleTable: ObjectMap<String, AbstractGrammarRule>, deferredRules: Array<DeferredRule>, namedAreas: ObjectMap<String, Array<Area>>)
	{
		val newArgs = RuleArguments()
		newArgs.ruleTable = ruleTable
		newArgs.deferredRules = deferredRules
		newArgs.area = areaMap[args.area]
		newArgs.defines = defineMap[args.defines]
		newArgs.variables = variableMap[args.variables]
		newArgs.symbolTable = symbolMap[args.symbolTable]
		newArgs.seed = args.seed
		newArgs.namedAreas = namedAreas

		rule.execute(newArgs)
	}

	companion object
	{
		val areaMap = ObjectMap<Area, Area>()
		val defineMap = ObjectMap<ObjectMap<String, String>, ObjectMap<String, String>>()
		val variableMap = ObjectMap<ObjectFloatMap<String>, ObjectFloatMap<String>>()
		val symbolMap = ObjectMap<ObjectMap<Char, GrammarSymbol>, ObjectMap<Char, GrammarSymbol>>()

		fun reset()
		{
			areaMap.clear()
			defineMap.clear()
			variableMap.clear()
			symbolMap.clear()
		}
	}
}