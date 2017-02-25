package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.renderable
import com.lyeeedar.Pathfinding.Pathfinder
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.SpaceSlot

enum class MovementType
{
	MOVE,
	LEAP,
	ROLL,
	CHARGE,
	TELEPORT
}

class MoveSourceAction : AbstractTimelineAction()
{
	lateinit var type: MovementType

	override fun enter()
	{
		val src = parent.sourceTile!!
		val entity = src.contents[SpaceSlot.ENTITY]
		val pos = entity.pos() ?: return

		var dst = parent.destinationTiles.random()!!
		val path = Pathfinder(src.level.grid, src.x, src.y, dst.x, dst.y, entity.pos().size, entity).getPath(SpaceSlot.ENTITY) ?: return

		for (point in path)
		{
			val tile = src.level.getTile(point) ?: break
			if (!tile.contents.containsKey(SpaceSlot.ENTITY)) dst = tile
			else break
		}

		for (x in 0..pos.size-1)
		{
			for (y in 0..pos.size-1)
			{
				val tile = src.level.getTile(src, x, y)
				tile?.contents?.remove(pos.slot)
			}
		}

		pos.position = dst

		for (x in 0..pos.size-1)
		{
			for (y in 0..pos.size-1)
			{
				val tile = dst.level.getTile(dst, x, y)
				tile?.contents?.put(pos.slot, entity)
			}
		}

		entity.renderable().renderable.animation = MoveAnimation.obtain().set(dst, src, 0.15f)
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = MoveSourceAction()
		action.parent = parent
		action.type = type

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		type = MovementType.valueOf(xml.get("MovementType", "Move").toUpperCase())
	}
}