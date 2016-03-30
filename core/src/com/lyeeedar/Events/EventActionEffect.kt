package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.EffectComponent
import com.lyeeedar.Components.EventComponent
import com.lyeeedar.Components.PositionComponent
import com.lyeeedar.Enums
import com.lyeeedar.GlobalData
import com.lyeeedar.Level.Tile
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionEffect(group: EventActionGroup): AbstractEventAction(group)
{
	lateinit var spriteData: XmlReader.Element
	lateinit var fireData: XmlReader.Element
	lateinit var firePoint: Sprite.AnimationStage
	lateinit var eventType: EventComponent.EventType

	override fun handle(args: EventArgs, tile: Tile)
	{
		val entity = Entity()

		entity.add(PositionComponent(tile))

		val effect = EffectComponent(AssetManager.loadSprite(spriteData), Enums.Direction.CENTER)
		effect.eventMap.put(firePoint, EventArgs(eventType, args.receiver, args.receiver, 0f))
		entity.add(effect)

		val event = EventComponent()
		event.parse(fireData)
		entity.add(event)

		GlobalData.Global.engine?.addEntity(entity)
	}

	override fun parse(xml: XmlReader.Element)
	{

	}
}