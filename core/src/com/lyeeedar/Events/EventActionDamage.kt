package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.XmlReader

import com.lyeeedar.Components.*
import com.lyeeedar.ElementType
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Util.FastEnumMap

/**
 * Created by Philip on 29-Mar-16.
 */

class EventActionDamage(group: EventActionGroup): IteratingEventAction(group, Family.all(StatisticsComponent::class.java).get())
{
	constructor(group: EventActionGroup, stats: FastEnumMap<ElementType, Float>) : this(group)
	{
		for (elem in ElementType.Values)
		{
			damMap.put(elem, stats.get(elem) ?: 0f)
		}
	}

	val damMap: FastEnumMap<ElementType, Float> = ElementType.getElementMap()

	override fun handle(args: EventArgs, entity: Entity)
	{
		val stats = Mappers.stats.get(entity)

		var totalDam = 0f
		for (elem in ElementType.Values)
		{
			val atk = damMap.get(elem) ?: 0f
			if (atk == 0f) continue

			val def = stats.defense.get(elem) ?: 0f

			val dam = Math.max(0f, atk - def)

			totalDam += dam
		}

		if (totalDam > 0)
		{
			stats.hp -= totalDam
			System.out.println("Dam: " + totalDam)
			entity.postEvent(EventArgs(EventComponent.EventType.DAMAGED, null, entity, totalDam))

			val hitSprite = AssetManager.loadSprite("EffectSprites/Hit/Hit", 0.01f);
			val damEntity = Entity()
			damEntity.add(EffectComponent(hitSprite))
			damEntity.add(PositionComponent(entity.tile()!!))
			entity.tile()?.effects?.add(damEntity)

			GlobalData.Global.engine.addEntity(damEntity)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)

			val elem = ElementType.valueOf(el.name.toUpperCase())

			damMap.put(elem, el.text.toFloat())
		}
	}

}