package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Combo.Item
import com.lyeeedar.Combo.Weapon

class ComboComponent(): AbstractComponent()
{
	lateinit var combos: ComboTree
	var currentCombo: ComboTree? = null

	var comboSource: Weapon? = null

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		combos = ComboTree.load(xml.get("ComboTree"))
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		if (comboSource != null)
		{
			output.writeBoolean(true)
			kryo.writeObject(output, comboSource!!.loadData)
		}
		else
		{
			output.writeBoolean(false)
		}
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val hasItem = input.readBoolean()

		if (hasItem)
		{
			val xml = kryo.readObject(input, XmlReader.Element::class.java)
			val item = Item.load(xml) as Weapon
			comboSource = item
			combos = item.combos
		}
	}
}