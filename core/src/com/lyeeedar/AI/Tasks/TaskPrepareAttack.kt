package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 31-Mar-16.
 */

class TaskPrepareAttack(val minOffset: Point, val maxOffset: Point, val entity: Entity, val dir: Enums.Direction): AbstractTask(EventComponent.EventType.NONE)
{
	override fun execute(e: Entity)
	{
		val atk = Mappers.telegraphed.get(e)
		val etile = e.tile() ?: return
		val tile = etile.level.getTile(etile, minOffset) ?: return

		e.sprite().sprite.spriteAnimation = BumpAnimation.obtain().set(0.4f, dir.opposite);

		//tile.contents.put(Enums.SpaceSlot.AIR, entity)
		if (entity.pos() == null)
		{
			entity.add(PositionComponent())
		}
		entity.pos().min = tile
		entity.pos().max = etile + maxOffset
		entity.sprite().sprite.rotation = atk.currentDir.angle

		if (atk.currentDir == Enums.Direction.WEST || atk.currentDir == Enums.Direction.EAST)
		{
			entity.sprite().sprite.fixPosition = true
		}

		GlobalData.Global.engine.addEntity(entity)

		minOffset.free()
		maxOffset.free()
	}
}