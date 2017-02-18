package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.GenerationGrammar.Area
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.children
import com.lyeeedar.Util.toCharGrid
import java.util.*

class GrammarRulePrefab : AbstractGrammarRule()
{
	val symbols = Array<GrammarRuleSymbol>()
	lateinit var prefab: Array2D<Char>

	override fun execute(area: Area, ruleTable: ObjectMap<String, AbstractGrammarRule>, defines: ObjectMap<String, String>, variables: ObjectFloatMap<String>, symbolTable: ObjectMap<Char, GrammarSymbol>, ran: Random)
	{
		val newSymbols = ObjectMap<Char, GrammarSymbol>()
		symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }

		for (symbol in symbols)
		{
			symbol.execute(area, ruleTable, defines, variables, newSymbols, ran)
		}

		val newArea = area.copy()

		val oldWidth = newArea.width
		val oldHeight = newArea.height

		newArea.width = prefab.width
		newArea.height = prefab.height

		val diffX = newArea.width - oldWidth
		val diffY = newArea.height - oldHeight

		newArea.x -= diffX / 2
		newArea.y -= diffY / 2

		if (newArea.isPoints)
		{
			newArea.points.clear()
			newArea.addPointsWithin(area)

			for (x in 0..prefab.width-1)
			{
				for (y in 0..prefab.height-1)
				{
					if (newArea.points.contains(Pos(newArea.x + x, newArea.y + y)))
					{
						val char = prefab[x, y]
						val symbol = newSymbols[char]

						val tile = newArea[x, y] ?: continue
						tile.write(symbol)
					}
				}
			}
		}
		else
		{
			for (x in 0..prefab.width-1)
			{
				for (y in 0..prefab.height-1)
				{
					val char = prefab[x, y]
					val symbol = newSymbols[char]

					val tile = newArea[x, y] ?: continue
					tile.write(symbol)
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
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

		prefab = xml.getChildByName("Prefab").toCharGrid()
	}
}