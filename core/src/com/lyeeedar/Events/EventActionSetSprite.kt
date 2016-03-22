package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.AssetManager
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.SpriteComponent
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionSetSprite(group: EventActionGroup): NameRestrictedEventAction(group, null)
{
	var spriteData: XmlReader.Element? = null

	override fun handleEntity(args: EventArgs, entity: Entity)
	{
		if (spriteData == null)
		{
			entity.remove(SpriteComponent::class.java)
		}
		else
		{
			entity.add(SpriteComponent(AssetManager.loadSprite(spriteData)))
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityName = xml.getAttribute("Entity", "this")
		spriteData = if (xml.name.toUpperCase() == "SETSPRITE") xml else null
	}

}