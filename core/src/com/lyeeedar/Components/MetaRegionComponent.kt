package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import ktx.collections.addAll

class MetaRegionComponent :  AbstractComponent()
{
	val keys = Array<String>()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		keys.addAll(xml.get("Key").toLowerCase().split(','))
	}

}
