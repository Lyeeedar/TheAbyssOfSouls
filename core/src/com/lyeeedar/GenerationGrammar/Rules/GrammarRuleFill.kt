package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.SpaceSlot
import java.util.*

class GrammarRuleFill : AbstractGrammarRule()
{
	var char: Char = ' '

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random)
	{
		val symbolToWrite = symbolTable[char]

		for (pos in area.getAllPoints())
		{
			val symbol = area[pos.x - area.x, pos.y - area.y] ?: continue

			symbol.write(symbolToWrite)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		char = xml.get("Character")[0]
	}

}
