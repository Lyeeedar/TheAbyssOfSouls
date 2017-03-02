package com.lyeeedar.Interaction

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.children

class InteractionNode
{
	val actions = Array<AbstractInteractionAction>()

	fun parse(xml: XmlReader.Element)
	{
		for (el in xml.children())
		{
			val action = AbstractInteractionAction.load(el)
			actions.add(action)
		}
	}

	fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{
		for (action in actions)
		{
			action.resolve(nodes)
		}
	}
}
