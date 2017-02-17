package com.lyeeedar.Combo

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.pos
import com.lyeeedar.Direction
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getXml
import ktx.collections.set

abstract class ComboStep
{
	lateinit var name: String

	var anim: String = "attack"
	var canTurn: Boolean = false

	abstract fun activate(entity: Entity, direction: Direction, target: Point)
	abstract fun getAllValid(entity: Entity, direction: Direction): Array<Point>
	abstract fun isValid(entity: Entity, direction: Direction, target: Point): Boolean
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): ComboStep
		{
			val step: ComboStep = when (xml.getAttribute("meta:RefKey").toUpperCase())
			{
				"WAIT" -> WaitComboStep()
				"SCENE" -> SceneTimelineComboStep()
				else -> throw NotImplementedError("Unknown combo step type: " + xml.name.toUpperCase())
			}

			step.name = xml.get("Name")
			step.anim = xml.get("Anim", "attack")
			step.canTurn = xml.getBoolean("CanTurn", false)

			step.parse(xml)

			return step
		}
	}
}

class ComboTree
{
	lateinit var current: ComboStep
	val next = Array<ComboTree>()

	companion object
	{
		fun load(path: String): Array<ComboTree>
		{
			val xml = getXml(path)
			return load(xml)
		}

		private fun load(xml: XmlReader.Element): Array<ComboTree>
		{
			val combosEl = xml.getChildByName("Combos")
			val descMap = ObjectMap<String, ComboStep>()

			for (el in combosEl.children())
			{
				val desc = ComboStep.load(el)
				descMap[desc.name.toUpperCase()] = desc
			}

			val root = ComboTree()

			fun recursiveParse(el: XmlReader.Element, parent: ComboTree)
			{
				val descName = el.get("Desc")
				val desc = descMap[descName.toUpperCase()]

				val current = ComboTree()
				current.current = desc

				parent.next.add(current)

				val nodesEl = el.getChildByName("Nodes")
				if (nodesEl != null)
				{
					for (child in nodesEl.children())
					{
						recursiveParse(child, current)
					}
				}
			}

			val rootEl = xml.getChildByName("Root")
			for (el in rootEl.children())
			{
				recursiveParse(el, root)
			}

			return root.next
		}
	}
}