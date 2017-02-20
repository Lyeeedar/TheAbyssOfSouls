package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.children
import ktx.collections.set
import java.util.*

class GrammarRuleSymbol : AbstractGrammarRule()
{
	var char: Char = ' '
	var extends: Char? = null
	val contents = FastEnumMap<SpaceSlot, XmlReader.Element>(SpaceSlot::class.java)

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random, deferredRules: Array<DeferredRule>)
	{
		val symbol = GrammarSymbol(char)

		if (extends != null)
		{
			val existing = symbolTable[extends]

			symbol.write(existing)
		}

		symbol.write(contents)

		symbolTable[char] = symbol
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