package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.renderable
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

class InteractionActionChangeSprite : AbstractInteractionAction()
{
	lateinit var sprite: Sprite

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		parent.renderable().renderable = sprite

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}