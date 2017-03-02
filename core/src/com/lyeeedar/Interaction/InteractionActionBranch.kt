package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.evaluate
import com.lyeeedar.Util.children
import com.lyeeedar.Util.round

class InteractionActionBranch : AbstractInteractionAction()
{
	val branches = Array<BranchNode>()

	override fun interact(entity: Entity, interaction: Interaction): Boolean
	{
		val variables = interaction.getVariables(entity)
		for (branch in branches)
		{
			if (branch.condition.evaluate(variables).round() == 1)
			{
				if (branch.hasNode)
				{
					interaction.interactionStack.add(InteractionNodeData(branch.node, 0))
				}

				break
			}
		}

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (el in xml.children())
		{
			val condition = el.get("Condition", "1")
			var key = el.get("Node", null)
			var hasNode = true

			if (key == null)
			{
				hasNode = false
				key = ""
			}

			branches.add(BranchNode(condition, key, hasNode))
		}
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{
		for (branch in branches)
		{
			if (branch.hasNode)
			{
				branch.node = nodes[branch.key]
			}
		}
	}
}

data class BranchNode(val condition: String, val key: String, val hasNode: Boolean)
{
	lateinit var node: InteractionNode
}