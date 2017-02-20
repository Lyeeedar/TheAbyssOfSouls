package com.lyeeedar.Systems

import com.badlogic.ashley.core.EntitySystem
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.renderable
import com.lyeeedar.Components.water
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot

class TileSystem : EntitySystem(systemList.indexOf(TileSystem::class))
{
	var level: Level? = null
		get() = field
		set(value)
		{
			field = value
		}

	override fun update(deltaTime: Float)
	{
		for (tile in level!!.grid)
		{
			processWater(tile)
		}
	}

	fun processWater(tile: Tile)
	{
		val entity = tile.contents[SpaceSlot.ENTITY] ?: return
		val sprite = entity.renderable()?.renderable as? Sprite ?: return

		val water = tile.contents[SpaceSlot.FLOOR]?.water()

		if (water == null)
		{
			sprite.removeAmount = 0.0f
			return
		}

		if (sprite.animation == null)
		{
			sprite.removeAmount = 0.3f
		}
	}
}
