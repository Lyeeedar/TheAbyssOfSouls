package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.children
import ktx.collections.set

class GrammarRuleSymbol : AbstractGrammarRule()
{
	var char: Char = ' '
	var extends: Char? = null
	val contents = FastEnumMap<SpaceSlot, XmlReader.Element>(SpaceSlot::class.java)

	suspend override fun execute(args: RuleArguments)
	{
		val symbol = GrammarSymbol(char)

		if (extends != null)
		{
			val existing = args.symbolTable[extends]

			symbol.write(existing)
		}

		symbol.write(contents)

		args.symbolTable[char] = symbol
	}

	override fun parse(xml: XmlReader.Element)
	{
		char = xml.get("Character")[0]
		extends = xml.get("Extends", null)?.first()

		for (el in xml.children())
		{
			if (el.name == "Character") continue
			if (el.name == "Extends") continue

			val slot = SpaceSlot.valueOf(el.name.toUpperCase())
			contents[slot] = el
		}
	}

}