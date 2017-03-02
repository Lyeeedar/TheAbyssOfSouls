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
import java.util.*

abstract class AbstractGrammarRule
{
	suspend abstract fun execute(area: Area,
								 ruleTable: ObjectMap<String, AbstractGrammarRule>,
								 defines: ObjectMap<String, String>,
								 variables: ObjectFloatMap<String>,
								 symbolTable: ObjectMap<Char, GrammarSymbol>,
								 ran: Random, deferredRules: Array<DeferredRule>)

	abstract fun parse(xml: XmlReader.Element)

	fun executeAsync(area: Area,
				ruleTable: ObjectMap<String, AbstractGrammarRule>,
				defines: ObjectMap<String, String>,
				variables: ObjectFloatMap<String>,
				symbolTable: ObjectMap<Char, GrammarSymbol>,
				ran: Random, deferredRules: Array<DeferredRule>): Job
	{
		val newDefines = ObjectMap<String, String>(defines.size)
		newDefines.putAll(defines)

		val newVariables = ObjectFloatMap<String>(variables.size)
		newVariables.putAll(variables)

		val newSymbols = ObjectMap<Char, GrammarSymbol>(symbolTable.size)
		symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }

		val newRan = Random(ran.nextLong())

		return launch(CommonPool) {
			execute(area, ruleTable, newDefines, newVariables, newSymbols, newRan, deferredRules)
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
				"ROTATE" -> GrammarRuleRotate()
				"RULE" -> GrammarRuleRule()
				"SELECT" -> GrammarRuleSelect()
				"SCALE" -> GrammarRuleScale()
				"SYMBOL" -> GrammarRuleSymbol()
				"TRANSLATE" -> GrammarRuleTranslate()

				else -> throw NotImplementedError("Unknown rule type: " + xml.name.toUpperCase())
			}

			rule.parse(xml)

			return rule
		}
	}
}