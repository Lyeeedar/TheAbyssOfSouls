package com.lyeeedar.DungeonGeneration.Data

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.SpaceSlot

/**
 * Created by Philip on 17-Apr-16.
 */

class SymbolicFeature()
{
	enum class Placement
	{
		HIDDEN, // Not visible from any doors
		CORNER, // Enclosed by a minimum of two neighbouring walls
		WALL, // Next to a solid wall
		CENTRE, // Not next to any solid walls
		ANY; // Any tile

		companion object
		{
			val Values: Array<Placement> = Placement.values()
		}
	}

	lateinit var entity: XmlReader.Element
	lateinit var slot: SpaceSlot
	lateinit var placement: Placement

	var overwrite: Boolean = false

	var useCount: Boolean = true
	var count: Int = 1
	var coverage: Float = 0.1f

	companion object
	{
		fun load(xml: XmlReader.Element): SymbolicFeature
		{
			val feature = SymbolicFeature()

			feature.entity = xml.getChildByName("Entity")
			feature.slot = SpaceSlot.valueOf(xml.get("Slot", "Entity").toUpperCase())

			feature.placement = Placement.valueOf(xml.get("Placement", "ANY").toUpperCase())

			feature.overwrite = xml.getChildByName("Overwrite") != null

			feature.useCount = xml.getChildByName("Count") != null
			feature.count = xml.getInt("Count", 1)
			feature.coverage = xml.getFloat("Coverage", 0.1f)

			return feature
		}
	}
}