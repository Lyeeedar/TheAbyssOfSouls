package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.renderable
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Pathfinding.BresenhamLine
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Animation.SpinAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.SpaceSlot

enum class MovementType
{
	MOVE,
	LEAP,
	ROLL,
	TELEPORT
}

class MoveSourceAction : AbstractTimelineAction()
{
	lateinit var type: MovementType

	override fun enter()
	{
		val src = parent.sourceTile!!
		val dst = parent.destinationTiles.random()!!

		doMove(src, dst, type)
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

class MoveDestAction : AbstractTimelineAction()
{
	lateinit var type: MovementType

	override fun enter()
	{
		for (src in parent.destinationTiles)
		{
			doMove(src, parent.sourceTile!!, type)
		}
	}

	override fun exit()
	{

	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val action = MoveDestAction()
		action.parent = parent
		action.type = type

		return action
	}

	override fun parse(xml: XmlReader.Element)
	{
		type = MovementType.valueOf(xml.get("MovementType", "Move").toUpperCase())
	}
}

private fun doMove(src: Tile, dst: Tile, type: MovementType)
{
	val entity = src.contents[SpaceSlot.ENTITY] ?: return
	val pos = entity.pos() ?: return

	val path = BresenhamLine.lineNoDiag(src.x, src.y, dst.x, dst.y, src.level.grid)

	var dst = src
	for (point in path)
	{
		val tile = src.level.getTile(point) ?: break

		if (!pos.isValidTile(tile, entity)) break

		dst = tile
	}

	if (dst == src) return

	pos.doMove(dst, entity)

	if (type == MovementType.MOVE)
	{
		entity.renderable().renderable.animation = MoveAnimation.obtain().set(dst, src, 0.3f)
	}
	else if (type == MovementType.ROLL)
	{
		entity.renderable().renderable.animation = MoveAnimation.obtain().set(dst, src, 0.3f)

		val direction = Direction.getDirection(src, dst)
		if (direction == Direction.EAST)
		{
			entity.renderable().renderable.animation = SpinAnimation.obtain().set(0.3f, -360f)
		}
		else
		{
			entity.renderable().renderable.animation = SpinAnimation.obtain().set(0.3f, 360f)
		}

		entity.renderable().renderable.animation = ExpandAnimation.obtain().set(0.2f, 1f, 0.8f, false)
	}
	else if (type == MovementType.LEAP)
	{
		entity.renderable().renderable.animation = LeapAnimation.obtain().setRelative(0.3f, src, dst, 2f)

		if (entity.renderable().renderable is Sprite)
		{
			(entity.renderable().renderable as Sprite).removeAmount = 0f
		}
	}
}