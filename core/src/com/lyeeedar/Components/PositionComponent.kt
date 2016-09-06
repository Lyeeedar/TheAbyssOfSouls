package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class PositionComponent: Component
{
	constructor()
	constructor(point: Point, slot: SpaceSlot = SpaceSlot.ENTITY)
	{
		this.position = point
		this.max = point
		this.slot = slot
	}

	var position: Point = Point() // bottom left pos
		set(value)
		{
			facing = Direction.getCardinalDirection(value.x - field.x, value.y - field.y)

			field = value
			max = value
			turnsOnTile = 0
		}
	var min: Point
		set(value) { position = value }
		get() { return position }
	var max: Point = Point()
	var slot: SpaceSlot = SpaceSlot.ENTITY
	var size: Int = 1
	var canSwap: Boolean = false

	var facing: Direction = Direction.SOUTH

	var turnsOnTile: Int = 0

	val x: Int
		get() = position.x
	val y: Int
		get() = position.y

	fun hasEffects() = hasEffects(position)

	fun hasEffects(direction: Direction): Boolean
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