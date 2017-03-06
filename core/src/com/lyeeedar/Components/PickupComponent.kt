package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.Item

class PickupComponent : AbstractComponent()
{
	var item: Item? = null

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}
}