package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class DialogueComponent : AbstractComponent()
{
	var text: String? = null
	var displayedText: String? = null


	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}
}