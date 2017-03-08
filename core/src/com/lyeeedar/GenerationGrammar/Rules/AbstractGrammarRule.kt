package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch

abstract class AbstractGrammarRule
{
	suspend abstract fun execute(args: RuleArguments)

	abstract fun parse(xml: XmlReader.Element)

	fun executeAsync(args: RuleArguments): Job
	{
		val cpy = args.copy(false, true, true, true)

		cpy.area.allowedBoundsX = cpy.area.x
		cpy.area.allowedBoundsY = cpy.area.y
		cpy.area.allowedBoundsWidth = cpy.area.width
		cpy.area.allowedBoundsHeight = cpy.area.height

		return launch(CommonPool) {
			execute(cpy)
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractGrammarRule
		{
			val rule: AbstractGrammarRule = if (xml.name == "Node") GrammarRuleNode() else when (xml.getAttribute("meta:RefKey").toUpperCase())
			{
				"CONDITION" -> GrammarRuleCondition()
				"DATASCOPE" -> GrammarRuleDataScope()
				"DEFINE" -> GrammarRuleDefine()
				"DEFER" -> GrammarRuleDefer()
				"DIVIDE" -> GrammarRuleDivide()
				"FILL" -> GrammarRuleFill()
				"FILTER" -> GrammarRuleFilter()
				"FLIP" -> GrammarRuleFlip()
				"REPEAT" -> GrammarRuleRepeat()
				"PREFAB" -> GrammarRulePrefab()
				"NAMEDAREA" -> GrammarRuleNamedArea()
				"ROTATE" -> GrammarRuleRotate()
				"RULE" -> GrammarRuleRule()
				"SCALE" -> GrammarRuleScale()
				"SPLIT" -> GrammarRuleSplit()
				"SYMBOL" -> GrammarRuleSymbol()
				"TAKE" -> GrammarRuleTake()
				"TRANSLATE" -> GrammarRuleTranslate()

				else -> throw NotImplementedError("Unknown rule type: " + xml.name.toUpperCase())
			}

			rule.parse(xml)

			return rule
		}
	}
}

class RuleArguments
{
	lateinit var area: Area
	lateinit var ruleTable: ObjectMap<String, AbstractGrammarRule>
	lateinit var defines: ObjectMap<String, String>
	lateinit var variables: ObjectFloatMap<String>
	lateinit var symbolTable: ObjectMap<Char, GrammarSymbol>
	var seed: Long = 0
	lateinit var deferredRules: Array<DeferredRule>
	lateinit var namedAreas: ObjectMap<String, Array<Area>>

	fun copy(scopeArea: Boolean = false, scopeDefines: Boolean = false, scopeVariables: Boolean = false, scopeSymbols: Boolean = false): RuleArguments
	{
		val args = RuleArguments()

		args.ruleTable = ruleTable
		args.seed = seed
		args.deferredRules = deferredRules
		args.namedAreas = namedAreas

		if (scopeArea)
		{
			args.area = area.copy()
		}
		else
		{
			args.area = area
		}

		if (scopeDefines)
		{
			args.defines = ObjectMap<String, String>()
			args.defines.putAll(defines)
		}
		else
		{
			args.defines = defines
		}

		if (scopeVariables)
		{
			args.variables = ObjectFloatMap()
			args.variables.putAll(variables)
		}
		else
		{
			args.variables = variables
		}

		if (scopeSymbols)
		{
			args.symbolTable = ObjectMap()
			symbolTable.forEach { args.symbolTable.put(it.key, it.value.copy()) }
		}
		else
		{
			args.symbolTable = symbolTable
		}

		return args
	}
}