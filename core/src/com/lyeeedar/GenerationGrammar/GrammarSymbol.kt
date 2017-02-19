package com.lyeeedar.GenerationGrammar

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.FastEnumMap

class GrammarSymbol(var char: Char)
{
	val contents = FastEnumMap<SpaceSlot, XmlReader.Element>(SpaceSlot::class.java)

	fun write(data: FastEnumMap<SpaceSlot, XmlReader.Element>)
	{
		for (slot in SpaceSlot.Values)
		{
			if (data.containsKey(slot))
			{
				contents[slot] = data[slot]
			}
		}
	}

	fun write(data: GrammarSymbol, overwrite: Boolean = false)
	{
		if (overwrite)
		{
			contents.clear()
		}

		write(data.contents)
		char = data.char
	}

	fun copy(): GrammarSymbol
	{
		val symbol = GrammarSymbol(char)

		symbol.write(contents)

		return symbol
	}
}
