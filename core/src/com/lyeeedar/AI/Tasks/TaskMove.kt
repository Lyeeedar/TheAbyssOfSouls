package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.math.Interpolation
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.SpaceSlot

class TaskMove(var direction: Direction): AbstractTask()
{
	override fun execute(e: Entity)
	{
		e.directionalSprite()?.currentAnim = "move"

		val pos = e.pos() ?: return

		if (pos.position is Tile)
		{
			val prev = (pos.position as Tile)
			val next = prev.level.getTile(prev, direction) ?: return

			if (e.pos().isValidTile(next, e))
			{
				e.pos().doMove(next, e)

				e.renderable().renderable.animation = MoveAnimation.obtain().set(next, prev, 0.15f)
			}
		}
		else
		{
			pos.position.x += direction.x
			pos.position.y += direction.y
		}

		if (e.stats() != null) e.stats().stamina += 2
	}
}