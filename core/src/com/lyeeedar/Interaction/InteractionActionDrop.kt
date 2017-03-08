package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.Item
import com.lyeeedar.Components.DropComponent
import com.lyeeedar.Components.tile

class InteractionActionDrop : AbstractInteractionAction()
{
	lateinit var item: Item

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		val tile = parent.tile()!!
		DropComponent.dropTo(tile, tile, item)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		val el = xml.getChildByName("Drop")
		item = Item.load(el)
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
