package com.lyeeedar.Components

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Sprite.DirectionalSprite
import com.lyeeedar.Util.AssetManager

class DirectionalSpriteComponent() : AbstractComponent()
{
	lateinit var directionalSprite: DirectionalSprite

	var currentAnim: String = "idle"
	var lastV: DirectionalSprite.VDir = DirectionalSprite.VDir.DOWN
	var lastH: DirectionalSprite.HDir = DirectionalSprite.HDir.RIGHT

	override fun parse(xml: XmlReader.Element, entity: Entity)
	{
		directionalSprite = AssetManager.loadDirectionalSprite(xml, entity.pos()?.size ?: 1)
	}
}
