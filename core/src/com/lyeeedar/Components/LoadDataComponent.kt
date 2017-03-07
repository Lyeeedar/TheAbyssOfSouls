package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class LoadDataComponent(val xml: XmlReader.Element) : AbstractComponent()
{
	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}

}