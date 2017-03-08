package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Combo.Item

class PickupComponent : AbstractComponent()
{
	lateinit var item: Item

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{

	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		kryo.writeClassAndObject(output, item.loadData)
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val xml = kryo.readClassAndObject(input) as XmlReader.Element
		item = Item.load(xml)
	}
}