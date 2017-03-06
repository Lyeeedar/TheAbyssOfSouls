package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

abstract class AbstractInteractionAction
{
	abstract fun interact(entity: Entity, interaction: Interaction): Boolean
	abstract fun parse(xml: XmlReader.Element)
	abstract fun resolve(nodes: ObjectMap<String, InteractionNode>)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractInteractionAction
		{
			val action = when (xml.getAttribute("meta:RefKey").toUpperCase())
			{
				"BRANCH" -> InteractionActionBranch()
				"LINE" -> InteractionActionLine()
				"DEFINE" -> InteractionActionDefine()
				"NODE" -> InteractionActionNode()
				"CHOICE" -> InteractionActionChoice()
				"DROP" -> InteractionActionDrop()

				else -> throw Exception("Unknown action type '" + xml.getAttribute("meta:RefKey") + "'!")
			}

			action.parse(xml)

			return action
		}
	}
}