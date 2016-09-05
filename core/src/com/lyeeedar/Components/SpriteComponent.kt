package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 20-Mar-16.
 */

class SpriteComponent(): Component
{
	constructor(sprite: Sprite)
		: this()
	{
		this.sprite = sprite
	}

	var sprite: Sprite = AssetManager.loadSprite("blank")
}