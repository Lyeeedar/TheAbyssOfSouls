package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.DialogueComponent
import com.lyeeedar.Components.dialogue
import com.lyeeedar.Components.hasComponent
import com.lyeeedar.Global
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.UI.ButtonKeyboardHelper
import com.lyeeedar.UI.addClickListener
import com.lyeeedar.Util.children
import ktx.scene2d.table
import ktx.scene2d.textButton

class InteractionActionChoice : AbstractInteractionAction()
{
	lateinit var text: String
	val choices = Array<Choice>()

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
				dialogue.displayedText = dialogue.text
				return true
			}
		}

		val keyboardHelper = ButtonKeyboardHelper()

		val table = table {
			for (choice in choices)
			{
				textButton(choice.text, "default", Global.skin) { cell ->
					cell.growX()
					this.addClickListener {
						this@table.remove()
						(Global.game.screen as AbstractScreen).keyboardHelper = null

						if (!choice.key.isNullOrBlank())
						{
							interaction.interactionStack.add(com.lyeeedar.Interaction.InteractionNodeData(choice.node, 0))
						}

						dialogue.remove = true

						interaction.interact(activating, parent)
					}

					keyboardHelper.add(this)
				}
				this.row()
			}
		}

		(Global.game.screen as AbstractScreen).keyboardHelper = keyboardHelper

		table.pack()

		Global.stage.addActor(table)
		table.setPosition(Global.stage.width/2f - table.width/2f, 20f )

		interaction.interactionStack.last().index++
		return false
	}

	override fun parse(xml: XmlReader.Element)
	{
		text = xml.get("Text")

		val choicesEl = xml.getChildByName("Choices")
		for (el in choicesEl.children())
		{
			val text = el.get("Text")
			val key = el.get("Node", "")

			choices.add(Choice(text, key))
		}
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{
		for (choice in choices)
		{
			if (!choice.key.isNullOrBlank())
			{
				choice.node = nodes[choice.key]
			}
		}
	}
}

data class Choice(val text: String, val key: String)
{
	lateinit var node: InteractionNode
}