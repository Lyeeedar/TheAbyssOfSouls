package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Level.World
import com.lyeeedar.Systems.level
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Colour

class InteractionActionChangeLevel : AbstractInteractionAction()
{
	lateinit var level: String
	lateinit var fadeColour: Colour

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		World.world.changeLevel(level, level, Global.engine.level!!.player, fadeColour)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		level = xml.get("Level")

		val colEl = xml.getChildByName("FadeColour")
		if (colEl != null)
		{
			fadeColour = AssetManager.loadColour(colEl)
		}
		else
		{
			fadeColour = Colour.BLACK
		}
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}