package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.ObjectMap

class Interaction
{
	val interactionStack = Array<InteractionNode>()
	lateinit var root: InteractionActionBranch
	val nodes = ObjectMap<String, InteractionNode>()

	val variableMap = ObjectFloatMap<String>()

	fun interact(entity: Entity): Boolean
	{
		if (interactionStack.size == 0)
		{
			root.interact(entity, this)
		}

		while (true)
		{
			if (interactionStack.size == 0) break
			val current = interactionStack.last()
			val action = current.actions[current.index]

			val advance = action.interact(entity, this)
			if (advance)
			{
				current.index++
				if (current.index == current.actions.size)
				{
					interactionStack.removeValue(current, true)
				}
			}
			else
			{
				break
			}
		}

		return interactionStack.size > 0
	}

	fun reset()
	{
		interactionStack.clear()
		for (node in nodes.values())
		{
			node.index = 0
		}
	}

	fun getVariables(entity: Entity): ObjectFloatMap<String>
	{
		return variableMap
	}
}
