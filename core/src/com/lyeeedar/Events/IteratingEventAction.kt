package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.Level.Tile

/**
 * Created by Philip on 22-Mar-16.
 */

abstract class IteratingEventAction(group: EventActionGroup, val family: Family?): AbstractEventAction(group)
{
	override fun handle(args: EventArgs, tile: Tile)
	{
		for (entity in tile.contents)
		{
			if (family?.matches(entity) ?: true)
			{
				handle(args, entity)
			}
		}
	}

	abstract fun handle(args: EventArgs, entity: Entity)
}