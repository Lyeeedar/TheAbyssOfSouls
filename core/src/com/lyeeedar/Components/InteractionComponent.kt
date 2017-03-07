package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectFloatMap
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Interaction.Interaction

class InteractionComponent : AbstractComponent()
{
	lateinit var interaction: Interaction

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		interaction = Interaction.Companion.load(xml.get("Interaction"))
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		kryo.writeObject(output, interaction.variableMap)
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val variables = kryo.readObject(input, ObjectFloatMap::class.java) as ObjectFloatMap<String>
		variables.forEach { interaction.variableMap.put(it.key, it.value) }
	}
}