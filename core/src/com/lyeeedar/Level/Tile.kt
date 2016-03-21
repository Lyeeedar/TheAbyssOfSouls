package com.lyeeedar.Level

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Pathfinding.PathfindingTile
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class Tile() : Point(0, 0), PathfindingTile
{
    val neighbours: FastEnumMap<Enums.Direction, Tile> = FastEnumMap( Enums.Direction::class.java )
    val contents: FastEnumMap<Enums.SpaceSlot, Entity> = FastEnumMap( Enums.SpaceSlot::class.java )
	lateinit var level: Level
	var visible: Boolean = false
	var seen: Boolean = false

    override fun getInfluence(travelType: EnumBitflag<Enums.SpaceSlot>, self: Any?) = 0

    override fun getPassable(travelType: EnumBitflag<Enums.SpaceSlot>, self: Any?): Boolean
    {
        if (contents.get(Enums.SpaceSlot.WALL) != null) { return false; }

        for (slot in Enums.SpaceSlot.values())
        {
            if (travelType.contains(slot))
            {
                val obj = contents.get(slot)
                if (obj != self)
                {
                    return false;
                }
            }
        }

        return true;
    }

	fun getPosDiff(p: Point): FloatArray
	{
		val oldPos = floatArrayOf(p.x * GlobalData.Global.tileSize, p.y * GlobalData.Global.tileSize)
		val newPos = floatArrayOf(x * GlobalData.Global.tileSize, y * GlobalData.Global.tileSize)

		return floatArrayOf(oldPos[0]-newPos[0], oldPos[1]-newPos[1])
	}
}