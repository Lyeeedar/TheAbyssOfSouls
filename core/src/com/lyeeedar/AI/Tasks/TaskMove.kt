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
		val renderable = e.renderable()

		if (pos.position is Tile)
		{
			val prev = (pos.position as Tile)

			// check valid
			var isValidMove = true
			outer@ for (x in 0..pos.size-1)
			{
				for (y in 0..pos.size-1)
				{
					val tile = prev.level.getTile(prev, x+direction.x, y+direction.y)
					if (tile == null || (tile.contents.get(pos.slot) != null && tile.contents.get(pos.slot) != e) || tile.contents.get(SpaceSlot.WALL) != null)
					{
						isValidMove = false
						break@outer
					}
				}
			}

			if (isValidMove)
			{
				for (x in 0..pos.size-1)
				{
					for (y in 0..pos.size-1)
					{
						val tile = prev.level.getTile(prev, x, y)
						tile?.contents?.remove(pos.slot)
					}
				}

				val next = prev.level.getTile(prev, direction) ?: return
				pos.position = next

				for (x in 0..pos.size-1)
				{
					for (y in 0..pos.size-1)
					{
						val tile = next.level.getTile(next, x, y)
						tile?.contents?.put(pos.slot, e)
					}
				}

				renderable.renderable.animation = MoveAnimation.obtain().set(next, prev, 0.15f)
			}
		}
		else
		{
			pos.position.x += direction.x;
			pos.position.y += direction.y;
		}

		if (e.stats() != null) e.stats().stamina += 2
	}
}