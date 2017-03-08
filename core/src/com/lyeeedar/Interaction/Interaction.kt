package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.sin
import com.lyeeedar.Global
import com.lyeeedar.Sin
import com.lyeeedar.Systems.level
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getXml
import ktx.collections.set

class Interaction
{
	val interactionStack = Array<InteractionNodeData>()
	lateinit var root: InteractionActionBranch
	val nodes = ObjectMap<String, InteractionNode>()

	val variableMap = ObjectFloatMap<String>()

	fun interact(activating: Entity, parent: Entity)
	{
		if (interactionStack.size == 0)
		{
			root.interact(activating, parent, this)
		}

		while (true)
		{
			if (interactionStack.size == 0) break
			val current = interactionStack.last()
			if (current.index == current.node.actions.size)
			{
				interactionStack.removeValue(current, true)
				continue
			}

			val action = current.node.actions[current.index]

			val advance = action.interact(activating, parent, this)
			if (advance)
			{
				current.index++
				if (current.index == current.node.actions.size)
				{
					interactionStack.removeValue(current, true)
				}
			}
			else
			{
				break
			}
		}

		if (interactionStack.size == 0)
		{
			Global.interaction = null
		}
	}

	fun reset()
	{
		interactionStack.clear()
	}

	fun getVariables(): ObjectFloatMap<String>
	{
		val player = Global.engine.level!!.player
		val sins = player.sin()

		variableMap.put("sinful", 0f)
		for (sin in Sin.Values)
		{
			val sinVal = sins.sins[sin]?.toFloat() ?: 0f
			variableMap.put(sin.toString().toLowerCase(), sinVal)

			if (sinVal > 0f)
			{
				variableMap.put("sinful", 1f)
			}
		}

		return variableMap
	}

	companion object
	{
		fun load(path: String): Interaction
		{
			val xml = getXml("Interactions/$path")
			return load(xml)
		}

		fun load(xml: XmlReader.Element): Interaction
		{
			val interaction = Interaction()

			val rootEl = xml.getChildByName("Branch")
			val root = InteractionActionBranch()
			root.parse(rootEl)
			interaction.root = root

			val nodesEl = xml.getChildByName("Nodes")

			for (el in nodesEl.children())
			{
				val node = InteractionNode()
				node.parse(el)

				interaction.nodes[el.get("GUID")] = node
			}

			root.resolve(interaction.nodes)
			for (node in interaction.nodes.values())
			{
				node.resolve(interaction.nodes)
			}

			return interaction
		}
	}
}

data class InteractionNodeData(val node: InteractionNode, var index: Int)