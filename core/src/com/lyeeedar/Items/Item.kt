package com.lyeeedar.Items

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Enums
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
	var slot: Enums.EquipmentSlot? = null
	lateinit var type: String
	lateinit var stats: FastEnumMap<Enums.Statistic, Float>
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

			item.slot = if (xml.get("Slot", null) != null) Enums.EquipmentSlot.valueOf(xml.get("Slot").toUpperCase()) else null
			item.type = xml.get("Type", "")
			item.stats = Enums.Statistic.load(xml.getChildByName("Statistics"))
			item.hitSprite = loadSprite(xml.getChildByName("HitSprite"))

			item.value = xml.getInt("Value", 0)
			item.dropChance = xml.get("Drop", "1")

			return item
		}
	}
}