package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.DungeonGeneration.RoomGenerators.AbstractRoomGenerator
import com.lyeeedar.Enums
import com.lyeeedar.Util.Array2D
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class SymbolicRoomData()
{
	var spawnEquation: String = "1"

	var contents: Array2D<Char?> = Array2D()
	var width: String = "7"
	var height: String = "7"

	var x: Int = 0
	var y: Int = 0

	var lockRotation: Boolean = false
	var rotate: Boolean = false
	var flipVert: Boolean = false
	var flipHori: Boolean = false

	lateinit var ran: Random

	val widthVal: Int by lazy {
		Math.max(EquationHelper.evaluate(width, ran), 1f).toInt()
	}

	val heightVal: Int by lazy {
		Math.max(EquationHelper.evaluate(height, ran), 1f).toInt()
	}

	val symbolMap: ObjectMap<Char, Symbol> = ObjectMap()

	var placement: Enums.Direction = Enums.Direction.CENTRE
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

	fun copy() : SymbolicRoomData
	{
		val room = SymbolicRoomData()

		room.spawnEquation = spawnEquation
		room.contents = contents
		room.width = width
		room.height = height
		room.lockRotation = lockRotation
		room.rotate = rotate
		room.flipVert = flipVert
		room.flipHori = flipHori
		room.x = x
		room.y = y
		room.ran = ran
		room.symbolMap.putAll(symbolMap)
		room.placement = placement
		room.generator = generator

		return room
	}

	fun create(ran: Random, levelData: SymbolicLevelData): SymbolicRoom
	{
		val room = SymbolicRoom()
		room.fill(ran, this, levelData)

		return room
	}

	companion object
	{
		@JvmStatic fun load(xml: XmlReader.Element): SymbolicRoomData
		{
			val room = SymbolicRoomData()

			room.spawnEquation = xml.getAttribute("Condition", null)?.toLowerCase() ?: room.spawnEquation
			room.spawnEquation = xml.getAttribute("Count", null)?.toLowerCase() ?: room.spawnEquation

			room.placement = Enums.Direction.valueOf(xml.get("Placement", "Centre").toUpperCase())
			room.lockRotation = xml.getBoolean("LockRotation", false)

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