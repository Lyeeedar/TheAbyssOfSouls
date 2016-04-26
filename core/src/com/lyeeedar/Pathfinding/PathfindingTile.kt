package com.lyeeedar.Pathfinding

import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.EnumBitflag

/**
 * Created by Philip on 20-Mar-16.
 */

interface PathfindingTile
{
    fun getPassable(travelType: SpaceSlot, self: Any?): Boolean

    fun getInfluence(travelType: SpaceSlot, self: Any?): Int
}
