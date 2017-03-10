package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Combo.Item
import com.lyeeedar.Util.children

class InventoryComponent : AbstractComponent()
{
	val items = ObjectMap<String, Item>()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		val itemsEl = xml.getChildByName("Items")

		if (itemsEl != null)
		{
			for (el in itemsEl.children())
			{
				val item = Item.load(el)
				items.put(item.name, item)
			}
		}
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		kryo.writeClassAndObject(output, items)
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val savedItems = kryo.readClassAndObject(input) as ObjectMap<String, Item>
		items.putAll(savedItems)
	}
}