package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Level.World
import com.lyeeedar.Systems.level

class InteractionActionChangeLevel : AbstractInteractionAction()
{
	lateinit var level: String

	override fun interact(activating: Entity, parent: Entity, interaction: Interaction): Boolean
	{
		World.world.changeLevel(level, level, Global.engine.level!!.player)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		level = xml.get("Level")
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}