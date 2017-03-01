package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader

abstract class AbstractInteractionAction
{
	abstract fun interact(entity: Entity, interaction: Interaction): Boolean
	abstract fun parse(xml: XmlReader.Element)
	abstract fun resolve(nodes: ObjectMap<String, InteractionNode>)
}