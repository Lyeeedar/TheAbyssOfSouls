package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate

class InteractionActionDefine : AbstractInteractionAction()
{
	lateinit var key: String
	lateinit var value: String

	override fun interact(entity: Entity, interaction: Interaction): Boolean
	{
		val outVal = value.evaluate(interaction.getVariables(entity))
		interaction.variableMap.put(key, outVal)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
		value = xml.get("Value")
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
