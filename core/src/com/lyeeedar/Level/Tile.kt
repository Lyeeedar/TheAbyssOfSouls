package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Pathfinding.PathfindingTile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.*

/**
 * Created by Philip on 20-Mar-16.
 */

class Tile() : Point(0, 0), PathfindingTile
{
    val neighbours: FastEnumMap<Direction, Tile> = FastEnumMap( Direction::class.java )
    val contents: FastEnumMap<SpaceSlot, Entity> = FastEnumMap( SpaceSlot::class.java )
	val effects: com.badlogic.gdx.utils.Array<Entity> = com.badlogic.gdx.utils.Array<Entity>(false, 4)
	lateinit var level: Level
	var visible: Boolean = false
	var seen: Boolean = false
	val light: Colour = Colour()

    override fun getInfluence(travelType: SpaceSlot, self: Any?) = 0

    override fun getPassable(travelType: SpaceSlot, self: Any?): Boolean
    {
        if (contents.get(SpaceSlot.WALL) != null) { return false; }

		val obj = contents.get(travelType)
		if (obj != null && obj != self)
		{
			if (self is Entity && self.isAllies(obj))
			{
				if (self.pos().canSwap)
				{
					return true
				}
				else if (obj.pos().turnsOnTile < 3)
				{
					return true
				}
			}

			return false
		}

        return true
    }

	fun hasTriggerEffects(): Boolean
	{
		for (e in effects)
		{
			val effect = Mappers.effect.get(e)
			if (effect.eventMap.size > 0)
			{
				return true
			}
		}

		return false
	}
}