package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getRotation

class TrailingEntityComponent : AbstractComponent()
{
	var initialised = false

	var collapses = true
	val entities = Array<Entity>()
	val tiles = Array<Tile>()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		collapses = xml.getBoolean("Collapses", true)
		entities.add(entity)

		val renderablesEl = xml.getChildByName("Renderables")
		for (el in renderablesEl.children())
		{
			val renderable = AssetManager.loadRenderable(el)
			val trailEntity = Entity()
			trailEntity.add(RenderableComponent(renderable))
			trailEntity.add(this)
			trailEntity.add(PositionComponent())
			trailEntity.pos().slot = entity.pos().slot

			if (entity.stats() != null) trailEntity.add(entity.stats())
			if (entity.water() != null) trailEntity.add(entity.water())

			entities.add(trailEntity)
		}
	}

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