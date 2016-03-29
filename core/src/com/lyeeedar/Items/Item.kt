package com.lyeeedar.Items

import com.lyeeedar.Enums
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 29-Mar-16.
 */

class Item()
{
	// Basic stuff
	lateinit var name: String
	lateinit var description: String
	lateinit var icon: Sprite

	// Equipable stuff
	lateinit var slot: Enums.EquipmentSlot
	lateinit var type: String
	lateinit var stats: FastEnumMap<Enums.Statistic, Float>
	lateinit var hitSprite: Sprite
	// abilities


}