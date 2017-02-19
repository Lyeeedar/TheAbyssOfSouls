package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import java.util.*

abstract class AbstractGrammarRule
{
	abstract fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	abstract fun parse(xml: XmlReader.Element)

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