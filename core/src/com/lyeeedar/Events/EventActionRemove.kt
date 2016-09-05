package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.stats
import com.lyeeedar.Global

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionRemove(group: EventActionGroup): NameRestrictedEventAction(group, null)
{
	override fun handleEntity(args: EventArgs, entity: Entity)
	{
		if (entity.stats() != null)
		{
			entity.stats().hp = 0f
		}
		else
		{
			Global.engine.removeEntity(entity)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityName = xml.name
	}

}
