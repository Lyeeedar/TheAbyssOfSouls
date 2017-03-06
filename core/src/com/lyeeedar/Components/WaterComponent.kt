package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction

class WaterComponent : AbstractComponent()
{
	var flowTowards: String? = null
	var flowDir = Direction.CENTER
	var flowChance = 0.3f
	var depth: Float = 0.3f

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		flowTowards = xml.get("FlowTowards", null)
		depth = xml.getFloat("Depth", 0.3f)
		flowChance = xml.getFloat("FlowChance", 0f)

		val dirEl = xml.get("Direction", null)
		if (dirEl != null) flowDir = Direction.valueOf(dirEl.toUpperCase())

		val trailing = entity.trailing()
		if (trailing != null)
		{
			for (e in trailing.entities)
			{
				if (e != entity) e.add(this)
			}
		}
	}
}