package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.position
import com.lyeeedar.Components.sprite
import com.lyeeedar.Components.tile
import com.lyeeedar.Enums
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 31-Mar-16.
 */

class TaskPrepareAttack(val offset: Point, val entity: Entity): AbstractTask(EventComponent.EventType.NONE)
{
	override fun execute(e: Entity)
	{
		val etile = e.tile() ?: return
		val tile = etile.level.getTile(etile.x + offset.x, etile.y + offset.y) ?: return

		tile.contents.put(Enums.SpaceSlot.AIR, entity)
		entity.position().position = tile
		entity.sprite().sprite.rotation = Enums.Direction.getDirection(etile, tile).angle
	}
}