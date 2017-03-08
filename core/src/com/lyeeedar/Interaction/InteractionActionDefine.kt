package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Level.World

class InteractionActionDefine : AbstractInteractionAction()
{
	lateinit var key: String
	lateinit var value: String
	var isGlobal = false

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		val outVal = value.evaluate(interaction.getVariables())

		if (isGlobal)
		{
			World.world.globalVariables.put(key, outVal)
		}
		else
		{
			interaction.variableMap.put(key, outVal)
		}

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.get("Key")
		value = xml.get("Value")
		isGlobal = xml.getBoolean("Global", false)
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
