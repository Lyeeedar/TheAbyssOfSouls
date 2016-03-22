package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation

/**
 * Created by Philip on 22-Mar-16.
 */

class TaskMove(var direction: Enums.Direction): AbstractTask(EventComponent.EventType.MOVE)
{
	override fun execute(e: Entity)
	{
		val pos = Mappers.position.get(e) ?: return
		val sprite = Mappers.sprite.get(e)

		if (pos.position is Tile)
		{
			val prev = (pos.position as Tile)
			val next = prev.neighbours.get(direction)

			if (next != null && next.contents.get(pos.slot) == null && next.contents.get(Enums.SpaceSlot.WALL) == null)
			{
				prev.contents.remove(pos.slot)
				pos.position = next
				next.contents.put(pos.slot, e)

				sprite.sprite.spriteAnimation = MoveAnimation(0.15f, next.getPosDiff(prev), MoveAnimation.MoveEquation.LINEAR)
			}
		}
		else
		{
			pos.position.x += direction.x;
			pos.position.y += direction.y;
		}
	}
}