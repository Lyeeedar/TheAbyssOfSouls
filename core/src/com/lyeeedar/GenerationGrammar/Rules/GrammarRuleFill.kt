package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol

class GrammarRuleFill : AbstractGrammarRule()
{
	var char: Char = ' '
	var overwrite = false

	suspend override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, seed: Long, deferredRules: Array<DeferredRule>)
	{
		val symbolToWrite = symbolTable[char]

		for (pos in area.getAllPoints())
		{
			val symbol = area[pos.x - area.x, pos.y - area.y] ?: continue

			symbol.write(symbolToWrite, overwrite)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		char = xml.get("Character")[0]
		overwrite = xml.getBoolean("Overwrite", false)
	}

}
