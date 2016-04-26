package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Pathfinding.PathfindingTile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.neaten
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class Symbol(var char: Char): PathfindingTile
{
	var extends: Char? = null

	val contents: FastEnumMap<SpaceSlot, XmlReader.Element> = FastEnumMap<SpaceSlot, XmlReader.Element>(SpaceSlot::class.java)

	var passable: Boolean = true
	var influence: Int = 0

	companion object
	{
		@JvmStatic fun load(xml: XmlReader.Element): Symbol
		{
			val symbol = Symbol(xml.get("Char").elementAt(0))
			symbol.extends = xml.getAttribute("Extends", null)?.elementAt(0)

			for (slot in SpaceSlot.Values)
			{
				val el = xml.getChildByName(slot.toString().neaten())
				if (el != null)
				{
					symbol.contents.put(slot, el)
				}
			}

			return symbol
		}
	}

	fun resolve(symbolMap: ObjectMap<Char, Symbol>)
	{
		if (extends != null)
		{
			val symbol = symbolMap.get(extends) ?: throw RuntimeException("Attempted to use undefined symbol '$extends'")
			extends = null

			symbol.resolve(symbolMap)

			for (slot in SpaceSlot.Values)
			{
				if (symbol.contents.containsKey(slot) && !contents.containsKey(slot))
				{
					contents.put(slot, symbol.contents[slot])
				}
			}
		}
	}

	fun copy(): Symbol
	{
		val symbol = Symbol(char)
		symbol.extends = extends

		for (slot in SpaceSlot.Values)
		{
			symbol.contents.put(slot, contents[slot])
		}

		return symbol
	}

	override fun getPassable(travelType: SpaceSlot, self: Any?): Boolean = passable
	override fun getInfluence(travelType: SpaceSlot, self: Any?): Int = influence
}