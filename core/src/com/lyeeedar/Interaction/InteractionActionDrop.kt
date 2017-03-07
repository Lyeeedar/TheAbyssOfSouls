package com.lyeeedar.Interaction

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Combo.Item
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.SpinAnimation
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.random
import ktx.collections.set

class InteractionActionDrop : AbstractInteractionAction()
{
	lateinit var item: Item

	override fun interact(entity: Entity, interaction: Interaction): Boolean
	{
		val tile = entity.tile()!!
		val targetTile = tile.neighbours.filter { it.getPassable(SpaceSlot.BELOWENTITY, null) && it.getPassable(SpaceSlot.ENTITY, null) }.asSequence().random()

		val dropEntity = Entity()
		dropEntity.add(RenderableComponent(item.icon))

		val particle = AssetManager.loadParticleEffect("PickupTwinkle")
		val additional = AdditionalRenderableComponent()
		additional.above["twinkle"] = particle
		dropEntity.add(additional)

		val pos = PositionComponent()
		pos.slot = SpaceSlot.BELOWENTITY
		pos.position = targetTile!!
		dropEntity.add(pos)

		val pickup = PickupComponent()
		pickup.item = item
		item.floorEntity = dropEntity
		dropEntity.add(pickup)

		targetTile.contents[SpaceSlot.BELOWENTITY] = dropEntity

		dropEntity.renderable().renderable.animation = LeapAnimation.obtain().setRelative(0.5f, tile, targetTile, 4f)
		dropEntity.renderable().renderable.animation = SpinAnimation.obtain().set(0.5f, 360f)
		dropEntity.renderable().renderable.animation = ExpandAnimation.obtain().set(0.2f, 0f, 1f, true)

		Global.engine.addEntity(dropEntity)

		return true
	}

	override fun parse(xml: XmlReader.Element)
	{
		val itemName = xml.get("Item")
		item = Item.load(itemName)
	}

	override fun resolve(nodes: ObjectMap<String, InteractionNode>)
	{

	}
}
