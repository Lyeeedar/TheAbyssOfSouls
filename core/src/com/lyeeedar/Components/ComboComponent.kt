package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.lyeeedar.Combo.ComboTree
import com.lyeeedar.Combo.Item
import com.lyeeedar.Combo.Weapon
import com.lyeeedar.Util.getXml

class ComboComponent(): AbstractComponent()
{
	lateinit var combos: ComboTree
	var currentCombo: ComboTree? = null

	var comboSource: Weapon? = null

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		val refxml = getXml(xml.get("ComboTree"))

		if (refxml.name == "Weapon")
		{
			val weapon = Item.load(refxml) as Weapon
			comboSource = weapon
		}

		combos = ComboTree.load(refxml)
	}

	override fun saveData(kryo: Kryo, output: Output)
	{
		output.writeBoolean(comboSource != null)

		if (comboSource != null)
		{
			kryo.writeClassAndObject(output, comboSource!!.loadData)
		}
	}

	override fun loadData(kryo: Kryo, input: Input)
	{
		val hasItem = input.readBoolean()

		if (hasItem)
		{
			val xml = kryo.readClassAndObject(input) as XmlReader.Element
			val item = Item.load(xml) as Weapon
			comboSource = item
			combos = item.combos
		}
	}
}