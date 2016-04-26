package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.EquipmentSlot
import com.lyeeedar.Items.Item
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 30-Mar-16.
 */

class InventoryComponent: Component
{
	constructor()

	val equipment: FastEnumMap<EquipmentSlot, Item> = FastEnumMap(EquipmentSlot::class.java)
	val items: com.badlogic.gdx.utils.Array<Item> = com.badlogic.gdx.utils.Array()
}