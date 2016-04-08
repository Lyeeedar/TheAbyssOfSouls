package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator
import com.lyeeedar.Enums
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.array2dOfChar
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicRoomData()
{
	var spawnEquation: String = "1"

	var contents: Array2D<Char?> = Array2D()
	var width: String = "5"
	var height: String = "5"

	lateinit var ran: Random

	val widthVal: Int by lazy {
		EquationHelper.evaluate(width, ran).toInt()
	}

	val heightVal: Int by lazy {
		EquationHelper.evaluate(height, ran).toInt()
	}

	val symbolMap: ObjectMap<Char, Symbol> = ObjectMap()

	var placement: Enums.Direction = Enums.Direction.CENTER
	var generator: AbstractRoomGenerator? = null

	fun resolveSymbols(sharedMap: ObjectMap<Char, Symbol>)
	{
		for (pair in sharedMap.entries())
		{
			if (!symbolMap.containsKey(pair.key))
			{
				symbolMap.put(pair.key, pair.value.copy())
			}
		}

		for (symbol in symbolMap.values())
		{
			symbol.resolve(symbolMap)
		}
	}

	fun create(ran: Random): SymbolicRoom
	{
		val room = SymbolicRoom()
		room.fill(ran, this)

		return room
	}

	companion object
	{
		fun load(xml: XmlReader.Element): SymbolicRoomData
		{
			val room = SymbolicRoomData()

			room.spawnEquation = xml.getAttribute("Condition", null)?.toLowerCase() ?: room.spawnEquation
			room.spawnEquation = xml.getAttribute("Count", null)?.toLowerCase() ?: room.spawnEquation

			room.placement = Enums.Direction.valueOf(xml.get("Placement", "Centre").toUpperCase())

			val rowsEl = xml.getChildByName("Rows")
			if (rowsEl != null)
			{
				var width = 0
				var height = rowsEl.childCount

				for (i in 0..height-1)
				{
					val row = rowsEl.getChild(i).text
					if (row.length > width)
					{
						width = row.length
					}
				}

				room.width = width.toString()
				room.height = height.toString()

				room.contents = Array2D<Char>(width, height)

				for (y in 0..height-1)
				{
					val row = rowsEl.getChild(y).text
					for (x in 0..width-1)
					{
						room.contents[x, height-y-1] = row.elementAt(x)
					}
				}
			}
			else
			{
				val generator = xml.getChildByName("Generator")
				if (generator != null)
				{
					room.generator = AbstractRoomGenerator.load(generator.getChild(0))
				}

				room.width = xml.get("Width", room.width)
				room.height = xml.get("Height", room.height)
			}

			val symbolsEl = xml.getChildByName("Symbols")
			if (symbolsEl != null)
			{
				for (i in 0..symbolsEl.childCount-1)
				{
					val el = symbolsEl.getChild(i)
					val symbol = Symbol.load(el)

					room.symbolMap.put(symbol.char, symbol)
				}
			}

			return room
		}
	}
}