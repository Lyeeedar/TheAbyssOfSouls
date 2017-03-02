package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

class InteractionActionNode : AbstractInteractionAction()
{
	lateinit var key: String
	lateinit var node: InteractionNode

	override fun interact(entity: Entity, interaction: Interaction): Boolean
	{
		interaction.interactionStack.add(InteractionNodeData(node, 0))
		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		key = xml.text
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{
		node = nodes[key]
	}
}