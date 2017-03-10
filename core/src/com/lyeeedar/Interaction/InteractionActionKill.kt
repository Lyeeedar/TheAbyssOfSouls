package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.MarkedForDeletionComponent

class InteractionActionKill : AbstractInteractionAction()
{
	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		parent.add(MarkedForDeletionComponent())

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}