package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import ktx.collections.set

class GrammarRuleDefine : AbstractGrammarRule()
{
	lateinit var key: String
	lateinit var value: String

	suspend override fun execute(args: RuleArguments)
	{
		if (value == "area")
		{
			if (!args.namedAreas.containsKey(key))
			{
				args.namedAreas[key] = Array()
			}

			args.namedAreas[key].add(args.area.copy())
		}
		else
		{
			var expanded = value
			for (def in args.defines)
			{
				expanded = expanded.replace(def.key, def.value)
			}

			args.defines[key] = value

			try
			{
				args.area.writeVariables(args.variables)

				val value = expanded.toLowerCase().evaluate(args.variables, args.seed)
				args.variables.put(key.toLowerCase(), value)
			}
			catch (ex: Exception)
			{

			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
		if (xml.getBoolean("NamedRegion", false)) value = "area"
		else value = xml.get("Value").unescapeCharacters()

		when (key)
		{
			"size", "pos", "count", "x", "y", "width", "height" -> throw UnsupportedOperationException("Define is using reserved name '$key'!")
		}
	}
}
