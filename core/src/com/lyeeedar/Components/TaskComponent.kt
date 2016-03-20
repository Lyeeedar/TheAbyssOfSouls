package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.lyeeedar.AI.IAI
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation

/**
 * Created by Philip on 20-Mar-16.
 */

class TaskComponent(val ai: IAI): Component()
{
	val tasks: com.badlogic.gdx.utils.Array<AbstractTask> = com.badlogic.gdx.utils.Array<AbstractTask>()
	var actionDelay: Float = 0f
}

abstract class AbstractTask()
{
	var duration: Float = 1f

	abstract fun execute(e: Entity)
}

class TaskMove(var direction: Enums.Direction): AbstractTask()
{
	override fun execute(e: Entity)
	{
		val pos = Mappers.position.get(e) ?: return
		val sprite = Mappers.sprite.get(e)

		if (pos.position is Tile)
		{
			val prev = (pos.position as Tile)
			val next = prev.neighbours.get(direction)

			if (next != null && next.contents.get(pos.slot) == null)
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

class TaskWait(): AbstractTask()
{
	override fun execute(e: Entity)
	{

	}
}