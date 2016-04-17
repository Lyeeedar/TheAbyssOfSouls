package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class PositionComponent: Component
{
	constructor()
	constructor(point: Point) { this.position = point; this.max = point }

	var position: Point = Point() // bottom left pos
		set(value)
		{
			field = value
			max = value
		}
	var min: Point
		set(value) { position = value }
		get() { return position }
	var max: Point = Point()
	var slot: Enums.SpaceSlot = Enums.SpaceSlot.ENTITY
	var size: Int = 1

	fun hasEffects() = hasEffects(position)

	fun hasEffects(direction: Enums.Direction): Boolean
	{
		val etile = position as Tile? ?: return false
		etile.level.getTile(etile, direction) ?: return false

		return hasEffects(position)
	}

	fun hasEffects(point: Point): Boolean
	{
		val tile = point as Tile? ?: return false

		for (x in 0..size-1)
		{
			for (y in 0..size-1)
			{
				val t = tile.level.getTile(tile, x, y) ?: continue

				if (t.hasTriggerEffects()) return true
			}
		}

		return false
	}
}