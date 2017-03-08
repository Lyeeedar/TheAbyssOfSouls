package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.Item
import com.lyeeedar.Global
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.SpinAnimation
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.children
import com.lyeeedar.Util.random
import ktx.collections.set

class DropComponent : AbstractComponent()
{
	val drops = Array<DropData>()

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		for (el in xml.children())
		{
			val chance = el.getFloat("Chance", 1f)
			val itemEl = el.getChildByName("Drop")
			val item = Item.load(itemEl)

			drops.add(DropData(chance, item))
		}
	}

	companion object
	{
		fun dropTo(tile: Tile, sourceTile: Tile, item: Item)
		{
			var targetTile = tile.neighbours.filter { it.getPassable(SpaceSlot.BELOWENTITY, null) && it.getPassable(SpaceSlot.ENTITY, null) }.asSequence().random()

			if (targetTile == null)
			{
				targetTile = tile.neighbours.filter { it.getPassable(SpaceSlot.BELOWENTITY, null) }.asSequence().random()
			}

			if (targetTile == null)
			{
				System.err.println("Failed to drop item '" + item.name + "'!")
				return
			}

			addToTile(targetTile, sourceTile, item)
		}

		fun addToTile(tile: Tile, sourceTile: Tile, item: Item)
		{
			val dropEntity = Entity()
			dropEntity.add(RenderableComponent(item.icon))

			val particle = AssetManager.loadParticleEffect("PickupTwinkle")
			val additional = AdditionalRenderableComponent()
			additional.above["twinkle"] = particle
			dropEntity.add(additional)

			val pos = PositionComponent()
			pos.slot = SpaceSlot.BELOWENTITY
			pos.position = tile
			dropEntity.add(pos)

			val pickup = PickupComponent()
			pickup.item = item
			dropEntity.add(pickup)

			tile.contents[SpaceSlot.BELOWENTITY] = dropEntity

			dropEntity.renderable().renderable.animation = LeapAnimation.obtain().setRelative(0.5f, sourceTile, tile, 4f)
			dropEntity.renderable().renderable.animation = SpinAnimation.obtain().set(0.5f, 360f)
			dropEntity.renderable().renderable.animation = ExpandAnimation.obtain().set(0.2f, 0f, 1f, true)

			Global.engine.addEntity(dropEntity)
		}
	}
}

data class DropData(val chance: Float, val item: Item)