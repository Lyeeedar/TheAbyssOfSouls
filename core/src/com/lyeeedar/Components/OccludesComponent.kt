package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class OccludesComponent : AbstractComponent()
{
	var occludes = true

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		occludes = xml.getBoolean("Occludes", true)
	}
}