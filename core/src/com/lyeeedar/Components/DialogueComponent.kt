package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader

class DialogueComponent : AbstractComponent()
{
	var text: String = ""
	var displayedText: String = ""

	var textAccumulator: Float = 0f
	var textFade: Float = 0.5f

	var turnsToShow = -1

	val alpha: Float
		get() = textFade / 0.5f

	var remove: Boolean = false
		set(value)
		{
			field = value
			textFade = 0.5f
		}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}
}