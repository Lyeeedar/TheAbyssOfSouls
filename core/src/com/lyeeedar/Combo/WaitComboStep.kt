package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.pos
import com.lyeeedar.Components.tile
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.Point

class WaitComboStep: ComboStep()
{
	override fun activate(entity: Entity, direction: Direction, target: Point)
	{

	}

	override fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	{
		val out = Array<Point>()
		out.add(entity.pos().position)
		return out
	}

	override fun isValid(entity: Entity, direction: Direction, target: Point): Boolean = true

	override fun parse(xml: XmlReader.Element)
	{

	}
}