package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import ktx.collections.set
import java.util.*

class GrammarRuleDefine : AbstractGrammarRule()
{
	lateinit var key: String
	lateinit var value: String

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		var expanded = value
		for (def in defines)
		{
			expanded = expanded.replace(def.key, def.value)
		}

		defines[key] = value

		try
		{
			area.writeVariables(variables)

			val value = expanded.toLowerCase().evaluate(variables, ran)
			variables.put(key.toLowerCase(), value)
		}
		catch (ex: Exception)
		{

		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
		value = xml.get("Value")

		when (key)
		{
			"size", "pos", "count", "x", "y", "width", "height" -> throw UnsupportedOperationException("Define is using reserved name '$key'!")
		}
	}
}
