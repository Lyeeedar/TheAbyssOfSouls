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

class TaskPrepareAttack(val point: Point, val entity: Entity, val dir: Enums.Direction): AbstractTask(EventComponent.EventType.NONE)
{
	override fun execute(e: Entity)
	{
		val atk = Mappers.telegraphed.get(e)
		val etile = e.tile() ?: return
		val tile = etile.level.getTile(point) ?: return

		e.sprite().sprite.spriteAnimation = BumpAnimation(0.4f, dir.opposite);

		//tile.contents.put(Enums.SpaceSlot.AIR, entity)
		if (entity.position() == null)
		{
			entity.add(PositionComponent())
		}
		entity.position().position = tile
		entity.sprite().sprite.rotation = atk.currentDir.angle

		GlobalData.Global.engine?.addEntity(entity)
	}
}