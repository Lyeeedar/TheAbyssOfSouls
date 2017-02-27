package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Util.getRotation

class TrailingEntityComponent : Component
{
	var initialised = false

	var collapses = true
	val entities = Array<Entity>()
	val tiles = Array<Tile>()

	fun updatePos(tile: Tile)
	{
		var target = tile
		for (i in 0..entities.size-1)
		{
			val entity = entities[i]
			entity.pos().tile = target

			if (tiles.size <= i)
			{
				tiles.add(target)
				if (i > 0)
				{
					Global.engine.addEntity(entities[i])
				}
			}
			else
			{
				val prevTile = tiles[i]
				tiles[i] = target

				if (prevTile.contents[entity.pos().slot] == entity) prevTile.contents[entity.pos().slot] = null
				if (!target.contents.containsKey(entity.pos().slot)) target.contents[entity.pos().slot] = entity

				entity.renderable().renderable.rotation = getRotation(prevTile, target)
				entity.renderable().renderable.animation = MoveAnimation.obtain().set(target, prevTile, 0.15f)

				target = prevTile
			}
		}
	}
}