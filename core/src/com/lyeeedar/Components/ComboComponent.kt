package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.ComboTree

class ComboComponent(): AbstractComponent()
{
	lateinit var combos: ComboTree
	var currentCombo: ComboTree? = null

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		combos = ComboTree.load(xml.get("ComboTree"))
	}
}