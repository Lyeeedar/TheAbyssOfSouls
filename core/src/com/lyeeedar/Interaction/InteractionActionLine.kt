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

	override fun interact(entity: Entity, interaction: Interaction): Boolean
	{
		if (!entity.hasComponent(DialogueComponent::class.java))
		{
			entity.add(DialogueComponent())
		}

		val dialogue = entity.dialogue()

		if (dialogue.text != text)
		{
			dialogue.remove = false
			dialogue.text = text
			dialogue.displayedText = ""

			Global.controls.onInput += fun (key): Boolean
			{
				if (dialogue.displayedText != dialogue.text)
				{
					dialogue.displayedText = dialogue.text
					return false
				}
				else
				{
					Future.call({ interaction.interact(entity) }, 0f)

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
