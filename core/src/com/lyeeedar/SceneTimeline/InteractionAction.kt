package com.lyeeedar.SceneTimeline

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.DialogueComponent
import com.lyeeedar.Components.dialogue
import com.lyeeedar.Components.hasComponent
import com.lyeeedar.Global
import com.lyeeedar.Interaction.Interaction

class SpeechAction : AbstractTimelineAction()
{
	lateinit var text: String

	override fun enter()
	{
		val entity = parent.parentEntity ?: return

		if (!entity.hasComponent(DialogueComponent::class.java))
		{
			entity.add(DialogueComponent())
		}

		val dialogue = entity.dialogue()
		dialogue.text = text
		dialogue.displayedText = ""
		dialogue.turnsToShow = -1
		dialogue.remove = false
	}

	override fun exit()
	{
		val entity = parent.parentEntity ?: return
		val dialogue = entity.dialogue() ?: return

		if (dialogue.text == text)
		{
			dialogue.remove = true
		}
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = SpeechAction()
		out.parent = parent
		out.text = text

		out.startTime = startTime
		out.duration = duration

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		text = xml.get("Text")
	}
}

class InteractionAction : AbstractBlockerAction()
{
	lateinit var interaction: Interaction

	override fun enter()
	{
		val entity = parent.parentEntity ?: return

		Global.interaction = interaction

		Global.interactionChanged += fun(int): Boolean {
			isExited = true
			exit()

			return true
		}

		interaction.interact(entity)

		isBlocked = true
	}

	override fun exit()
	{
		isBlocked = false
	}

	override fun copy(parent: SceneTimeline): AbstractTimelineAction
	{
		val out = InteractionAction()
		out.parent = parent
		out.interaction = interaction

		out.startTime = startTime
		out.duration = duration

		return out
	}

	override fun parse(xml: XmlReader.Element)
	{
		interaction = Interaction.Companion.load(xml.get("Interaction"))
	}
}