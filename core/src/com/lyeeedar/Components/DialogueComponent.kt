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

//	val createCallstack: Array<StackTraceElement>
//
//	init
//	{
//		createCallstack = Thread.currentThread().getStackTrace()
//		if (Global.release) throw Exception("Debug code needs to be removed!")
//	}

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}
}