package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Generators.CellularAutomata
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.Util.Random
import com.lyeeedar.Util.children
import com.lyeeedar.Util.freeTS

class GrammarRuleGenerator : AbstractGrammarRule()
{
	enum class GeneratorType
	{
		CELLULARAUTOMATA
	}

	lateinit var type: GeneratorType
	val symbols = Array<GrammarRuleSymbol>()

	suspend override fun execute(args: RuleArguments)
	{
		val newSymbols = ObjectMap<Char, GrammarSymbol>()
		args.symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }

		val generator = when (type)
		{
			GeneratorType.CELLULARAUTOMATA -> CellularAutomata()
			else -> throw Exception("Unhandled generator type '$type'!")
		}

		val ran = Random.obtainTS(args.seed)

		generator.process(args.area, newSymbols['.'], newSymbols['#'], ran)

		ran.freeTS()
	}

	override fun parse(xml: XmlReader.Element)
	{
		type = GeneratorType.valueOf(xml.get ("Type", "CellularAutomata").toUpperCase())

		val symbolsEl = xml.getChildByName("Symbols")
		if (symbolsEl != null)
		{
			for (el in symbolsEl.children())
			{
				val symbol = GrammarRuleSymbol()
				symbol.parse(el)

				symbols.add(symbol)
			}
		}
	}
}
