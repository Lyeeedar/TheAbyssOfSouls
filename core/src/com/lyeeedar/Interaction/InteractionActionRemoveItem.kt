package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.exp4j.Helpers.unescapeCharacters
import com.lyeeedar.Components.inventory
import com.lyeeedar.Util.round

class InteractionActionRemoveItem : AbstractInteractionAction()
{
	lateinit var name: String
	lateinit var count: String

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		val inventory = activating.inventory()

		if (inventory != null)
		{
			val existing = inventory.items[name]

			if (existing != null)
			{
				if (count == "all")
				{
					inventory.items.remove(name)
				}
				else
				{
					val variables = interaction.getVariables()
					variables.put("count", existing.count.toFloat())

					val count = count.evaluate(variables).round()

					existing.count -= count

					if (existing.count <= 0) inventory.items.remove(name)
				}
			}
		}

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		name = xml.get("Name").toLowerCase()
		count = xml.get("Count", "All").toLowerCase().unescapeCharacters()
		count = count.replace("%", "#count")
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
