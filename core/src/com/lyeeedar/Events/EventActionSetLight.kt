package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.LightComponent

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionSetLight(group: EventActionGroup): NameRestrictedEventAction(group, null)
{
	var data: XmlReader.Element? = null;

	override fun parse(xml: XmlReader.Element)
	{
		entityName = xml.getAttribute("Entity", "this")

		data = if (xml.name.toUpperCase() == "SETLIGHT") xml else null
	}

	override fun handleEntity(args: EventArgs, entity: Entity)
	{
		val d = data
		if (d == null)
		{
			entity.remove(LightComponent::class.java)
		}
		else
		{
			entity.add(LightComponent(AssetManager.loadColour(d), d.getFloat("Distance") ))
		}
	}
}