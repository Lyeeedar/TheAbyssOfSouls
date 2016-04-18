package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.XmlReader

/**
 * Created by Philip on 11-Apr-16.
 */

class SymbolicCorridorData()
{
	enum class Style
	{
		NORMAL,
		WANDERING
	}

	lateinit var style: Style
	var width: Int = 1

	var centralConstant: SymbolicFeature? = null

	companion object
	{
		fun load(xml: XmlReader.Element): SymbolicCorridorData
		{
			val corridor = SymbolicCorridorData()

			corridor.style = Style.valueOf(xml.get("Style", "normal").toUpperCase())
			corridor.width = xml.getInt("Width", 1)

			val ccEl = xml.getChildByName("CentralConstant")
			if (ccEl != null)
			{
				corridor.centralConstant = SymbolicFeature.load(ccEl)
			}

			return corridor
		}
	}
}