package com.lyeeedar.Events

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.utils.XmlReader

import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.TilingSpriteComponent
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 22-Mar-16.
 */

class EventActionSetTilingSprite(group: EventActionGroup): NameRestrictedEventAction(group, null)
{
	var spriteData: XmlReader.Element? = null

	override fun handleEntity(args: EventArgs, entity: Entity)
	{
		if (spriteData == null)
		{
			entity.remove(TilingSpriteComponent::class.java)
		}
		else
		{
			entity.add(TilingSpriteComponent(AssetManager.loadTilingSprite(spriteData!!)))
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		entityName = xml.getAttribute("Entity", "this")
		spriteData = if (xml.name.toUpperCase() == "SETTILINGSPRITE") xml else null
	}

}