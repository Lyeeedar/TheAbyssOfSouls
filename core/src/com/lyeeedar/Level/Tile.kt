package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Components.isAllies
import com.lyeeedar.Components.occludes
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.trailing
import com.lyeeedar.Direction
import com.lyeeedar.Pathfinding.IPathfindingTile
import com.lyeeedar.SceneTimeline.SceneTimeline
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Colour
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

class Tile : Point(), IPathfindingTile
{
	lateinit var level: Level

	val contents: FastEnumMap<SpaceSlot, Entity> = FastEnumMap( SpaceSlot::class.java )
	val timelines = Array<SceneTimeline>(false, 2)

	val neighbours: FastEnumMap<Direction, Tile> = FastEnumMap( Direction::class.java )

	var isVisible = false
	var isSeen = false

	var isValidTarget = false
	var isValidHitPoint = false
	var isSelectedPoint = false

	val light = Colour()

	override fun getInfluence(travelType: SpaceSlot, self: Any?) = 0

	override fun getPassable(travelType: SpaceSlot, self: Any?): Boolean
	{
		if (travelType == SpaceSlot.LIGHT)
		{
			for (slot in SpaceSlot.Values)
			{
				val obj = contents.get(slot)
				if (obj != null && obj != self)
				{
					val occludes = obj.occludes()
					if (occludes != null)
					{
						if (occludes.occludes) return false
					}
					else if (slot == SpaceSlot.WALL)
					{
						return false
					}
				}
			}

			return true
		}
		else
		{
			if (contents.get(SpaceSlot.WALL) != null) { return false; }

			val obj = contents.get(travelType)
			if (obj != null && obj != self)
			{
				if (self is Entity)
				{
					if (self.isAllies(obj) && obj.pos().turnsOnTile < 3)
					{
						return true
					}
					else if (self.trailing()?.entities?.contains(obj) ?: false)
					{
						return true
					}
				}

				return false
			}

			return true
		}
	}
}