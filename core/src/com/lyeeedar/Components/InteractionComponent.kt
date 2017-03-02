package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Interaction.Interaction

class InteractionComponent : AbstractComponent()
{
	lateinit var interaction: Interaction

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		interaction = Interaction.Companion.load(xml.get("Interaction"))
	}
}