package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.XmlReader

class GrammarRuleFill : AbstractGrammarRule()
{
	var char: Char = ' '
	var overwrite = false

	suspend override fun execute(args: RuleArguments)
	{
		val symbolToWrite = args.symbolTable[char]

		for (pos in args.area.getAllPoints())
		{
			val symbol = args.area[pos.x - args.area.x, pos.y - args.area.y] ?: continue

			symbol.write(symbolToWrite, overwrite)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		char = xml.get("Character")[0]
		overwrite = xml.getBoolean("Overwrite", false)
	}

}
