package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.lyeeedar.Components.name

/**
 * Created by Philip on 22-Mar-16.
 */

abstract class NameRestrictedEventAction(group: EventActionGroup, family: Family?): IteratingEventAction(group, family)
{
	var entityName: String = ""

	override fun handle(args: EventArgs, entity: Entity)
	{
		val e = when (entityName)
		{
			"" -> entity
			"this", "self" -> args.receiver
			"sender" -> args.sender ?: return
			else -> if (entity.name() == entityName) entity else return
		}

		handleEntity(args, e)
	}

	abstract fun handleEntity(args: EventArgs, entity: Entity)
}