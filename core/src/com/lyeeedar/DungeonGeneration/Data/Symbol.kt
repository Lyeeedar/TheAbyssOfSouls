package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Enums
import com.lyeeedar.Util.neaten
import java.util.*

/**
 * Created by Philip on 08-Apr-16.
 */

class Symbol(val char: Char)
{
	var extends: Char? = null

	val contents: EnumMap<Enums.SpaceSlot, XmlReader.Element> = EnumMap<Enums.SpaceSlot, XmlReader.Element>(Enums.SpaceSlot::class.java)

	companion object
	{
		fun load(xml: XmlReader.Element): Symbol
		{
			val symbol = Symbol(xml.get("Char").elementAt(0))
			symbol.extends = xml.getAttribute("Extends", null)?.elementAt(0)

			for (slot in Enums.SpaceSlot.Values)
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

			for (slot in Enums.SpaceSlot.Values)
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

		for (slot in Enums.SpaceSlot.Values)
		{
			symbol.contents.put(slot, contents[slot])
		}

		return symbol
	}
}