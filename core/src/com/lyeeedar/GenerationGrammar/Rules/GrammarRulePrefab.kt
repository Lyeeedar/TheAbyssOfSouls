package com.lyeeedar.GenerationGrammar.Rules

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.GenerationGrammar.GrammarSymbol
import com.lyeeedar.GenerationGrammar.Pos
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.children
import com.lyeeedar.Util.toCharGrid

class GrammarRulePrefab : AbstractGrammarRule()
{
	val symbols = Array<GrammarRuleSymbol>()
	lateinit var prefab: Array2D<Char>

	var usePoints = false
	lateinit var snap: Direction

	suspend override fun execute(args: RuleArguments)
	{
		val newSymbols = ObjectMap<Char, GrammarSymbol>()
		args.symbolTable.forEach { newSymbols.put(it.key, it.value.copy()) }

		val newArgs = args.copy(false, false, false, false)
		newArgs.symbolTable = newSymbols

		for (symbol in symbols)
		{
			symbol.execute(newArgs)
		}

		val newArea = args.area.copy()

		if (args.area.isPoints && !usePoints)
		{
			val avgX = args.area.points.sumBy(Pos::x) / args.area.points.size
			val avgY = args.area.points.sumBy(Pos::y) / args.area.points.size

			newArea.x = avgX
			newArea.y = avgY
			newArea.width = 1
			newArea.height = 1
		}

		val oldWidth = newArea.width
		val oldHeight = newArea.height

		newArea.width = prefab.width
		newArea.height = prefab.height

		val diffX = newArea.width - oldWidth
		val diffY = newArea.height - oldHeight

		if (snap.x == 0)
		{
			newArea.x -= diffX / 2
		}
		else if (snap.x < 0)
		{

		}
		else if (snap.x > 0)
		{
			newArea.x = (newArea.x + oldWidth) - newArea.width
		}

		if (snap.y == 0)
		{
			newArea.y -= diffY / 2
		}
		else if (snap.y < 0)
		{

		}
		else if (snap.y > 0)
		{
			newArea.y = (newArea.y + oldHeight) - newArea.height
		}

		if (newArea.isPoints && usePoints)
		{
			newArea.points.clear()
			newArea.addPointsWithin(args.area)

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
		usePoints = xml.getBoolean("UsePoints", false)

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

		for (y in 0..prefab.height/2-1)
		{
			for (x in 0..prefab.width-1)
			{
				val v1 = prefab[x, y]
				val v2 = prefab[x, (prefab.height-1)-y]

				prefab[x, y] = v2
				prefab[x, (prefab.height-1)-y] = v1
			}
		}

		snap = Direction.valueOf(xml.get("Snap", "Center").toUpperCase())
	}
}