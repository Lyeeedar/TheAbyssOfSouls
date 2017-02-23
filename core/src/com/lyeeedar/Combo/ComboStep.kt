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
import com.lyeeedar.Util.*
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
	enum class ComboKey
	{
		ATTACKNORMAL,
		ATTACKSPECIAL,
		DEFENSE,
		DIRECTION
	}

	lateinit var current: ComboStep
	val random = Array<ComboTree>()
	var cost = 0
	val keybinding = FastEnumMap<ComboKey, ComboTree>(ComboKey::class.java)

	companion object
	{
		fun load(path: String): ComboTree
		{
			val xml = getXml(path)
			return load(xml)
		}

		private fun load(xml: XmlReader.Element): ComboTree
		{
			val isKeyBinding = xml.get("NextMode", "Random").toUpperCase() == "KEYBINDING"

			val combosEl = xml.getChildByName("Combos")
			val descMap = ObjectMap<String, ComboStep>()

			for (el in combosEl.children())
			{
				val desc = ComboStep.load(el)
				descMap[desc.name.toUpperCase()] = desc
			}

			val root = ComboTree()

			fun recursiveParse(el: XmlReader.Element): ComboTree
			{
				val descName = el.get("Desc", null)
				val desc = if (descName != null) descMap[descName.toUpperCase()] else null

				val cost = el.getInt("Cost", 0)

				val current = ComboTree()
				if (desc != null) current.current = desc
				current.cost = cost

				if (isKeyBinding)
				{
					val nodesEl = el.getChildByName("Keybinding")
					if (nodesEl != null)
					{
						for (childEl in nodesEl.children())
						{
							val child = recursiveParse(childEl)

							val key = ComboKey.valueOf(childEl.name.toUpperCase())
							current.keybinding[key] = child
						}
					}
				}
				else
				{
					val nodesEl = el.getChildByName("Random")
					if (nodesEl != null)
					{
						for (childEl in nodesEl.children())
						{
							val child = recursiveParse(childEl)
							current.random.add(child)
						}
					}
				}

				return current
			}

			return recursiveParse(xml)
		}
	}
}