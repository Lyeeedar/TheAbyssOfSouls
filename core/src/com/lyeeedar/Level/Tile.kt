package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Pathfinding.PathfindingTile
import com.lyeeedar.Util.*

/**
 * Created by Philip on 20-Mar-16.
 */

class Tile() : Point(0, 0), PathfindingTile
{
    val neighbours: FastEnumMap<Enums.Direction, Tile> = FastEnumMap( Enums.Direction::class.java )
    val contents: FastEnumMap<Enums.SpaceSlot, Entity> = FastEnumMap( Enums.SpaceSlot::class.java )
	val effects: com.badlogic.gdx.utils.Array<Entity> = com.badlogic.gdx.utils.Array<Entity>(false, 4)
	lateinit var level: Level
	var visible: Boolean = false
	var seen: Boolean = false
	val light: Colour = Colour()

    override fun getInfluence(travelType: Enums.SpaceSlot, self: Any?) = 0

    override fun getPassable(travelType: Enums.SpaceSlot, self: Any?): Boolean
    {
        if (contents.get(Enums.SpaceSlot.WALL) != null) { return false; }

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

			return false;
		}

        return true;
    }

	fun getPosDiff(p: Point): FloatArray
	{
		val oldPos = floatArrayOf(p.x * GlobalData.Global.tileSize, p.y * GlobalData.Global.tileSize)
		val newPos = floatArrayOf(x * GlobalData.Global.tileSize, y * GlobalData.Global.tileSize)

		return floatArrayOf(oldPos[0]-newPos[0], oldPos[1]-newPos[1])
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