package com.lyeeedar.Pathfinding

import com.lyeeedar.Enums
import com.lyeeedar.Util.EnumBitflag

/**
 * Created by Philip on 20-Mar-16.
 */

interface PathfindingTile
{
    fun getPassable(travelType: Enums.SpaceSlot, self: Any?): Boolean

    fun getInfluence(travelType: Enums.SpaceSlot, self: Any?): Int
}
