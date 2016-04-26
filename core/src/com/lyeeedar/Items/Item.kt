package com.lyeeedar.Items

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.ElementType
import com.lyeeedar.EquipmentSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.loadSprite

/**
 * Created by Philip on 29-Mar-16.
 */

class Item()
{
	// Basic stuff
	lateinit var name: String
	lateinit var description: String
	var icon: Sprite? = null

	// Equipable stuff
	var slot: EquipmentSlot? = null
	lateinit var type: String
	val attack: FastEnumMap<ElementType, Float> = ElementType.getElementMap(0f)
	val defense: FastEnumMap<ElementType, Float> = ElementType.getElementMap(0f)
	val power: FastEnumMap<ElementType, Float> = ElementType.getElementMap(1f)
	var hitSprite: Sprite? = null
	// abilities

	// Other stuff
	var value: Int = 0
	var dropChance: String = "1"

	companion object
	{
		fun load(xml: XmlReader.Element): Item
		{
			val item = Item()
			item.name = xml.get("Name", "")
			item.description = xml.get("Description", "")
			item.icon = loadSprite(xml.getChildByName("Icon"))

			item.slot = if (xml.get("Slot", null) != null) EquipmentSlot.valueOf(xml.get("Slot").toUpperCase()) else null
			item.type = xml.get("Type", "")

			val attack = xml.getChildByName("Attack")
			if (attack != null) item.attack.addAll(ElementType.load(attack))

			val defense = xml.getChildByName("Defense")
			if (defense != null) item.defense.addAll(ElementType.load(defense))

			item.hitSprite = loadSprite(xml.getChildByName("HitSprite"))

			item.value = xml.getInt("Value", 0)
			item.dropChance = xml.get("Drop", "1")

			return item
		}
	}
}