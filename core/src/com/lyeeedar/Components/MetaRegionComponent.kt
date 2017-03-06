package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class MetaRegionComponent :  AbstractComponent()
{
	lateinit var key: String

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		key = xml.get("Key")
	}

}
