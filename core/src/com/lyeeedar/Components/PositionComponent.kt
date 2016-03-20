package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class PositionComponent(): Component()
{
	var position: Point = Point()
	var slot: Enums.SpaceSlot = Enums.SpaceSlot.ENTITY
	var size: Int = 1

}