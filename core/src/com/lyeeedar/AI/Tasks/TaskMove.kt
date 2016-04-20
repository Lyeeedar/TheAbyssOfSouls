package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sound.SoundInstance
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Util.isAllies

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

			// check valid
			var isValidMove = true
			outer@ for (x in 0..pos.size-1)
			{
				for (y in 0..pos.size-1)
				{
					val tile = prev.level.getTile(prev, x+direction.x, y+direction.y)
					if (tile == null || (tile.contents.get(pos.slot) != null && tile.contents.get(pos.slot) != e) || tile.contents.get(Enums.SpaceSlot.WALL) != null)
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

				sprite.sprite.spriteAnimation = MoveAnimation.obtain().set(0.15f, next.getPosDiff(prev), MoveAnimation.MoveEquation.LINEAR)

				val sound = SoundInstance(AssetManager.loadSound("hit"), "Combat")
				sound.play(next)
			}
			else if (pos.canSwap && pos.size == 1)
			{
				val collisionTile = prev.level.getTile(prev, direction.x, direction.y)
				if (collisionTile != null && collisionTile.contents.get(Enums.SpaceSlot.WALL) == null)
				{
					// we collided with an entity
					val collisionEntity = collisionTile.contents.get(pos.slot)
					if (e.isAllies(collisionEntity))
					{
						// if allies then we can swap

						// First move us
						val next = prev.level.getTile(prev, direction) ?: return
						pos.position = next
						e.tile()?.contents?.remove(pos.slot)
						next.contents[pos.slot] = e
						sprite.sprite.spriteAnimation = MoveAnimation.obtain().set(0.15f, next.getPosDiff(prev), MoveAnimation.MoveEquation.LINEAR)

						// Then move other
						val opos = Mappers.position.get(collisionEntity)
						opos.position = prev
						prev.contents[opos.slot] = collisionEntity
						val osprite = Mappers.sprite.get(collisionEntity)
						osprite.sprite.spriteAnimation = MoveAnimation.obtain().set(0.15f, prev.getPosDiff(next), MoveAnimation.MoveEquation.LINEAR)
					}
				}
			}
		}
		else
		{
			pos.position.x += direction.x;
			pos.position.y += direction.y;
		}
	}
}