package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Components.isAllies
import com.lyeeedar.Components.pos
import com.lyeeedar.Direction
import com.lyeeedar.Pathfinding.IPathfindingTile
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Renderables.Sprite.SpriteWrapper
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

	var isOpaque = false
	var isVisible = false
	var isSeen = false

	var isValidTarget = false
	var isValidHitPoint = false
	var isSelectedPoint = false

	val light = Colour()

	override fun getInfluence(travelType: SpaceSlot, self: Any?) = 0

	override fun getPassable(travelType: SpaceSlot, self: Any?): Boolean
	{
		if (contents.get(SpaceSlot.WALL) != null) { return false; }

		val obj = contents.get(travelType)
		if (obj != null && obj != self)
		{
			if (self is Entity && self.isAllies(obj))
			{
				if (obj.pos().turnsOnTile < 3)
				{
					return true
				}
			}

			return false
		}

		return true
	}
}