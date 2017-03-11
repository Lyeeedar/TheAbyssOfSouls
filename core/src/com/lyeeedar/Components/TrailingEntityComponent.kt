package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.children

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
			trailEntity.pos().moveable = false

			Future.call(
					{
						trailEntity.pos().slot = entity.pos().slot
						if (entity.stats() != null) trailEntity.add(entity.stats())
						if (entity.water() != null) trailEntity.add(entity.water())
					}, 0f)

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

				if (prevTile != target)
				{
					//entity.renderable().renderable.rotation = getRotation(prevTile, target)
					entity.renderable().renderable.animation = MoveAnimation.obtain().set(target, prevTile, 0.15f)
				}
				else
				{
					if (!entities[0].renderable().renderable.visible)
					{
						entity.renderable().renderable.visible = false
					}
				}

				target = prevTile
			}
		}

		if (entities.all { !it.renderable().renderable.visible })
		{
			entities.forEach { it.add(MarkedForDeletionComponent()) }
		}
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		output.writeInt(entities.size)

		for (i in 0..entities.size-1)
		{
			val tile = if (tiles.size == 0) entities[0].tile()!! else if (tiles.size <= i) tiles.last() else tiles[i]

			output.writeInt(tile.x)
			output.writeInt(tile.y)
		}
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val count = input.readInt()
		if (count != entities.size) throw Exception("Mismatched count of trail! Expected '" + entities.size + "' but got '" + count + "'!")

		val points = Array<Point>(entities.size)

		for (i in 0..entities.size-1)
		{
			val x = input.readInt()
			val y = input.readInt()

			points.add(Point(x, y))
		}

		Future.call({
			val head = entities[0]
			if (head.pos().position != points[0]) System.err.println("Trail head isnt in the same place as it was saved!")

			for (i in 0..points.size-1)
			{
				val entity = entities[i]
				val point = points[i]

				val tile = head.tile()!!.level.getTile(point)!!

				if (tiles.size == i) tiles.add(tile)
				else tiles[i] = tile

				entity.pos().tile = tile

				tile.contents[entity.pos().slot] = entity

				if (i > 0) Global.engine.addEntity(entities[i])
			}
		}, 0f)
	}
}