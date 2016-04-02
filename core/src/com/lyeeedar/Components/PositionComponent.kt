package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class PositionComponent: Component
{
	constructor()
	constructor(point: Point) { this.position = point }

	var position: Point = Point() // bottom left pos
	var min: Point
		set(value) { position = value }
		get() { return position }
	var max: Point = Point()
	var slot: Enums.SpaceSlot = Enums.SpaceSlot.ENTITY
	var size: Int = 1
}