package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.DialogueComponent
import com.lyeeedar.Components.dialogue
import com.lyeeedar.Components.hasComponent
import com.lyeeedar.Global
import com.lyeeedar.Util.Future

class InteractionActionLine : AbstractInteractionAction()
{
	lateinit var text: String
	var italics = false

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		if (!parent.hasComponent(DialogueComponent::class.java))
		{
			parent.add(DialogueComponent())
		}

		val dialogue = parent.dialogue()

		if (dialogue.text != text)
		{
			dialogue.remove = false
			dialogue.text = text
			dialogue.displayedText = ""

			Global.controls.onInput += fun (key): Boolean
			{
				val controlKey = Global.controls.getKey(key.source, key.code)
				if (controlKey != null) Global.controls.consumeKeyPress(controlKey)

				if (dialogue.displayedText != dialogue.text)
				{
					dialogue.displayedText = dialogue.text
					return false
				}
				else
				{
					Future.call({ interaction.interact(activating, parent) }, 0f)

					return true
				}
			}

			return false
		}
		else
		{
			if (dialogue.displayedText == text)
			{
				dialogue.remove = true
				return true
			}
			else
			{
				return false
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		text = xml.get("Text")
		italics = xml.getBoolean("Italics", false)
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
