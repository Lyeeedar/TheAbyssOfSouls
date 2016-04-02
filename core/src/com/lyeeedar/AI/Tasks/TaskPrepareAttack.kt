package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.*
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 31-Mar-16.
 */

class TaskPrepareAttack(val point: Point, val entity: Entity): AbstractTask(EventComponent.EventType.NONE)
{
	override fun execute(e: Entity)
	{
		val atk = Mappers.telegraphed.get(e)
		val etile = e.tile() ?: return
		val tile = etile.level.getTile(point) ?: return

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